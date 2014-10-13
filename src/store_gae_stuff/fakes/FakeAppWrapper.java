package store_gae_stuff.fakes;

import static gae_store_space.OfyService.ofy;
import gae_store_space.ActiveDistributionGenKind;
import gae_store_space.ContentPageKind;
import gae_store_space.WordKind;

import java.util.List;

import com.googlecode.objectify.Key;

public class FakeAppWrapper {
	public static final String defaultPageName = "Korra";
	
	public static final String defaultGenName = "No";
	
	public FakeAppWrapper() {
		// пока создаем один раз и удаляем. классы могут менятся, лучше так, чтобы не было 
		//   конфликтов.
		//
  	ofy().delete().keys(ofy().load().type(ContentPageKind.class).keys()).now();
  	ofy().delete().keys(ofy().load().type(ActiveDistributionGenKind.class).keys()).now();
  	ofy().delete().keys(ofy().load().type(WordKind.class).keys()).now();
  	
  	// Own tables
  	// FIXME: GAE can't read file.
  	ContentPageKind p0 = new BuilderOneFakePage().buildContentPage(defaultPageName);
  	ofy().save().entity(p0).now();
  	
  	// TODO: А если здесь проверить сохранена ли, то иногда будет несохранена!
  	
  	// add generator
  	ActiveDistributionGenKind g = ActiveDistributionGenKind.create(p0.getRawDistribution());
  	ofy().save().entity(g).now();
  	p0.setGenerator(g);
  	ofy().save().entity(p0).now();
  	
  	// Try read
  	///*
  	// Скрыл, но должно работать!!
  	//List<ContentPageKind> pages = ofy().load().type(ContentPageKind.class).filter("name = ", defaultPageName).list();
  	
  	// FIXME: иногда страбатывает - почему - не ясно - список пуст, все вроде бы синхронно
  	//if (pages.isEmpty()) {
  	//	throw new IllegalStateException();
  	//}
  	
  	//if (pages.size() > 1) {
  	//	throw new IllegalStateException();
  	//}
  	//*/  
	}
	
	// FIXME: may be non thread safe. Да вроде бы должно быть база то потокобезопасная?
	public 
	//synchronized  // не в этом дело 
	ContentPageKind getPage(String name) {
		
		List<ContentPageKind> pages = 
    		ofy().load().type(ContentPageKind.class).filter("name = ", name).list();
    
    if (pages.size() != 1) {
  		throw new IllegalStateException();
  	}
    
    return pages.get(0);  // 1 item
	}

	private static FakeAppWrapper w = null;
	static {
		w = new FakeAppWrapper(); 
	}
	
	private static class Holder {
		static final FakeAppWrapper w = new FakeAppWrapper();
	}
	
	public static FakeAppWrapper getInstance() {
		return w;
	}
}
