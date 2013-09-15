package web_wrapper;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.gson.Gson;
import common.math.GeneratorAnyRandom;
import idx_coursors.*;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.HandlerList;
import org.eclipse.jetty.server.handler.ResourceHandler;
import org.eclipse.jetty.server.nio.SelectChannelConnector;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import through_functional.configurator.AppConfigurator;
import through_functional.configurator.ConfFileIsCorrupted;
import through_functional.configurator.NoFoundConfFile;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.*;


public class AppContainer {

  private final ImmutableList<ImmutableNodeAccessor> NODES;
  private final ImmutableNodeAccessor ACTIVE_NODE_ACCESSOR;
  private final GeneratorAnyRandom GENERATOR;

  static public ImmutableList<ImmutableNodeAccessor> getNodes(
    ImmutableSet<String> nameNodes, IFabricImmutableNodeAccessors fabric)
    throws ConfFileIsCorrupted, NoFoundConfFile {
    Map<String, String> report = new HashMap<String, String>();
    List<ImmutableNodeAccessor> accessors = new ArrayList<ImmutableNodeAccessor>();
    for (String node: nameNodes) {
      try {
        ImmutableNodeAccessor accessor = fabric.create(node);
        accessors.add(accessor);
      } catch (NodeIsCorrupted e) {
        report.put(node, "Is corrupted");
      } catch (NodeNoFound e) {
        report.put(node, "No found");
      }
    }
    return ImmutableList.copyOf(accessors);
  }

  // Index: нужно для маркеровки
  // Word: само слово
  // Translates:
  // Context:
  public ImmutableList<ImmutableList<ImmutableList<String>>> getPackageActiveNode() {

    List<String> rawKeys = new ArrayList<String>();
    List<ImmutableList<String>> values = new ArrayList<ImmutableList<String>>();
    Integer currentKey = GENERATOR.getCodeWord();

    // Добавляем, только если что-то есть
    ImmutableList<String> content = ACTIVE_NODE_ACCESSOR.getContent(currentKey);
    if (!content.isEmpty()) {
      rawKeys.add("content");
      values.add(content);
    }

    ImmutableList<String> translate = ImmutableList.of();
    if (!translate.isEmpty()) {
      rawKeys.add("translate");
      values.add(translate);
    }

    // Обазятельно!
    rawKeys.add("word");
    values.add(ImmutableList.of(ACTIVE_NODE_ACCESSOR.getWord(currentKey)));

    List<ImmutableList<String>> keys = new ArrayList<ImmutableList<String>>();
    keys.add(ImmutableList.copyOf(rawKeys));

    return ImmutableList.of(ImmutableList.copyOf(keys), ImmutableList.copyOf(values));
  }

  public AppContainer(ImmutableList<ImmutableNodeAccessor> nodes) {
    // В try сделать нельзя - компилятор будет ругаться не неинициализованность
    NODES = nodes;  // Должна упасть если при ошибке дошла до сюда
    ACTIVE_NODE_ACCESSOR = NODES.asList().get(0);

    // Коннектим генератор случайных чисел и акссессор
    List<Integer> distribution = ACTIVE_NODE_ACCESSOR.getDistribution();
    GENERATOR = GeneratorAnyRandom.create(distribution);
  }

  private static Server createServer() {
    Server server = new Server();

    SelectChannelConnector connector = new SelectChannelConnector();
    connector.setPort(8080);
    server.addConnector(connector);

    // Подключаем корень?
    ResourceHandler resourceHandler = new ResourceHandler();
    resourceHandler.setDirectoriesListed(true);
    resourceHandler.setWelcomeFiles(new String[]{ "index.html" });
    resourceHandler.setResourceBase("./web-pages");


    // Список обработчиков?
    HandlerList handlers = new HandlerList();
    ServletHandler handler = new ServletHandler();
    handler.addServletWithMapping("web_wrapper.AppContainer$Pkg", "/pkg");
    handlers.setHandlers(new Handler[] { resourceHandler, handler });
    // ! если не находи index.html Открывает вид папки!!

    // Подключаем к серверу
    server.setHandler(handlers);
    return server;


  }

  private static Server build() {
    Server server = new Server();

    SelectChannelConnector connector = new SelectChannelConnector();
    connector.setPort(8080);
    server.addConnector(connector);

    // Подключаем ресурсы
    ResourceHandler resourceHandler = new ResourceHandler();
    resourceHandler.setDirectoriesListed(true);
    resourceHandler.setWelcomeFiles(new String[]{ "index.html" });
    resourceHandler.setResourceBase("./web-pages");

    // Регистрируем обработчики
    ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS);
    context.setContextPath("/");

    // Сервлеты
    try {
      ImmutableSet<String> namesNodes = AppConfigurator.getRegisteredNodes().get();
      ImmutableList<ImmutableNodeAccessor> accessors = getNodes(
          namesNodes, new FabricImmutableNodeAccessors());

      //
      AppContainer container = new AppContainer(accessors);
      context.addServlet(new ServletHolder(new Pkg(container)),"/pkg");

      // Коннектим все
      HandlerList handlers = new HandlerList();
      handlers.setHandlers(new Handler[] { resourceHandler, context });
      server.setHandler(handlers);

    } catch (NoFoundConfFile e) {
      throw new RuntimeException();
    } catch (ConfFileIsCorrupted e) {
      throw new RuntimeException();
    }

    return server;
  }

  public static void main(String[] args) throws Exception {
    Server server = build();
    server.start();
    server.join();
  }
}

