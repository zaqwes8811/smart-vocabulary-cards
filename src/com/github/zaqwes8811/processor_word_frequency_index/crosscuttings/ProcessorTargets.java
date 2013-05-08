package com.github.zaqwes8811.processor_word_frequency_index.crosscuttings;

import com.google.common.base.Charsets;
import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import com.google.common.io.Closer;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: кей
 * Date: 29.04.13
 * Time: 21:10
 * To change this template use File | Settings | File Templates.
 */

// TODO(zaqwes): Как убрать боковые пробелы из строки без сплиттера и джоинера?
// TODO(zaqwes): Сделано очень плохо! Может для именвание узлов не испльзовать []
//   Guava and Python can remove spaces in begin and in end
// TODO(zaqwes): но вообще подумать над удалением заданных краевых символов строки
public class ProcessorTargets {
  /**
   * @param msg - начало периода
   //* @throws IllegalArgument – если начало периода указано после конца
   * @throws NullPointerException – если начало или конец периода нулевые
   */
  public static void print(Object msg) {
    System.out.println(msg);
  }
  public final static int NODE_NAME = 0;
  public static int INDEX_URL = 1;

  //
  public static int RESULT_NODE_NAME = 0;
  public static int RESULT_PATH = 1;
  public static int RESULT_FILENAME = 2;

  // @precond: разделители пути как в linux - '/'
  //
  // @return: Имя исходного файла+путь к хранилищу. Расширения нужно приляпать
  //   *.ptxt or *.meta
  public List<String> splitUrlToFilenameAndPath(String fullPathToFile) throws CrosscuttingsException {
    // Запрещаем windows-разделители
    if (fullPathToFile.indexOf('\\') != -1) {
      throw new CrosscuttingsException("Path content disabled separators. "+
        "Need use *nix format - '/'. Path - "+fullPathToFile+"; Pos - "+fullPathToFile.indexOf('\\'));
    }

    // Путь первую проверку прошел
    int lastIdx =  fullPathToFile.lastIndexOf('/');
    int lengthInString = fullPathToFile.length();

    // Разделяем и пакуем
    List<String> result = new ArrayList<String>();
    String fname = fullPathToFile.substring(lastIdx + 1, lengthInString);
    String pathToFile = fullPathToFile.substring(0, lastIdx);
    result.add(pathToFile);
    result.add(fname);
    return result;
  }

  public List<List<String>> runParser(String pathToTarget) throws CrosscuttingsException {
    // Строка задания = [Node name]*url*...
    List<List<String>> resultTargets = new ArrayList<List<String>>();
    try {
      Closer closer = Closer.create();
      try {
        BufferedReader in = closer.register(new BufferedReader(new FileReader(pathToTarget)));
        // Получаем строку задания
        while (true) {
          String s = in.readLine();
          if (s == null) break;

          Iterable<String> oneRawTarget =
              Splitter.on('*').trimResults().omitEmptyStrings().split(s);

          List<String> elements = Lists.newArrayList(oneRawTarget);
          String nodeName = elements.get(NODE_NAME);
          String url = elements.get(INDEX_URL);

          List<String> resultTarget = new ArrayList<String>();
          resultTarget.add(extractNodeName(nodeName));  // 0
          resultTarget.addAll(splitUrlToFilenameAndPath(url));  // 1, 2

          resultTargets.add(resultTarget);
        }
        return resultTargets;
      } catch (FileNotFoundException e) {
        e.printStackTrace();
        throw new CrosscuttingsException("File no found - "+pathToTarget);
      } catch (Throwable e) { // must catch Throwable
        throw closer.rethrow(e);
      } finally {
         closer.close();
      }
    } catch (IOException e) {
      e.printStackTrace();
      throw new CrosscuttingsException("Error on read file - "+pathToTarget);
    }
  }

  // TODO(zaqwes): impl. very bad!!
  private String extractNodeName(String line) {
    // Очищаем имя узла
    Joiner joiner = Joiner.on("").skipNulls();

    Iterable<String> purgeNode =
        Splitter.on('[').trimResults().omitEmptyStrings().split(line);

    String tmp = joiner.join(purgeNode);
    purgeNode =
      Splitter.on(']').trimResults().omitEmptyStrings().split(tmp);
    return joiner.join(purgeNode);
  }

  public String getIndexName() {
    String indexCfgFilename = "apps/targets/spider_extractor_target.json";
    try {
      Closer closer = Closer.create();
      try {
        FileReader reader = new FileReader(indexCfgFilename);
        print(reader.getEncoding());
        BufferedReader in = closer.register(new BufferedReader(reader));

        String s;
        StringBuilder readedBuffer = new StringBuilder();
        while ((s = in.readLine())!= null) {  // TODO(zaqwes) TOTH: может лучше разом прочитать?
          //readedBuffer.append(s);
          readedBuffer.append(s);
        }
        String jsonSettings = readedBuffer.toString();
        //print(jsonSettings);

        // Разбираем
        // TODO(zaqwes) TOTH: Может все конфигурирование через yaml сделать, хотя в json проще на веб
        // TODO(zaqwes) TOTH:   передавать, а задание я на веб буду передавать?
        Gson gson = new Gson();
        Type type = new TypeToken<HashMap<String, ArrayList<String>>>() {}.getType();
        HashMap<String, List<String>> settings = gson.fromJson(jsonSettings, type);
        //
        return settings.get("index name").get(0);
      } catch (Throwable e) { // must catch Throwable
        throw closer.rethrow(e);
      } finally {
        closer.close();
      }
    } catch (FileNotFoundException e) {
      e.printStackTrace();
    } catch (IOException e) {
      e.printStackTrace();
    }
    return "TEST";
  }
}
