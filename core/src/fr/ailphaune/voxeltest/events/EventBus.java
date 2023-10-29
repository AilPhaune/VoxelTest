package fr.ailphaune.voxeltest.events;

import java.util.HashMap;
import java.util.function.BiConsumer;

import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.reflect.ClassReflection;
import com.badlogic.gdx.utils.reflect.Method;
import com.badlogic.gdx.utils.reflect.ReflectionException;

public class EventBus {
	
	private HashMap<Class<? extends BaseEvent>, Array<BiConsumer<EventBus, BaseEvent>>> subscribers;
	
	public EventBus() {
		subscribers = new HashMap<>();
	}
	
	public boolean subscribeEvent(Class<? extends BaseEvent> eventClass, BiConsumer<EventBus, BaseEvent> consumer) {
		Array<BiConsumer<EventBus, BaseEvent>> consumers = subscribers.getOrDefault(eventClass, null);
		if(consumers == null) {
			consumers = new Array<>();
			subscribers.put(eventClass, consumers);
		}
		consumers.add(consumer);
		return true;
	}
	
	public <Data, Value> boolean subscribeEvent(Class<? extends BaseEvent> eventClass, Method method, Object instance) {
		if(instance == null && !method.isStatic()) {
			return false;
		}
		if(method.isVarArgs()) return false;
		Class<?>[] params = method.getParameterTypes();
		if(params.length != 2) return false;
		if(params[0] != getClass()) return false;
		if(params[1] != eventClass) return false;
		if(method.getReturnType() != void.class) return false;
		
		return subscribeEvent(eventClass, (bus, event) -> {
			try {
				method.invoke(instance, bus, event);
			} catch (ReflectionException e) {
				e.printStackTrace();
			}
		});
	}
	
	public boolean subscribeEvent(Class<? extends BaseEvent> eventClass, Method method) {
		return subscribeEvent(eventClass, method, null);
	}
	
	@SuppressWarnings("unchecked")
	public boolean subscribeEventListener(Class<?> eventListenerClass) {
		Method[] methods = ClassReflection.getMethods(eventListenerClass);
		boolean atLeastOne = false;
		for(Method method : methods) {
			if(!method.isStatic() || method.isAbstract()) continue;
			if(!method.isAnnotationPresent(SubscribeEvent.class)) continue;
			

			if(method.isVarArgs()) {
				System.out.println("Warning: Couldn't register event subscriber " + method.getName() + " of class " + eventListenerClass + ": Invalid number of arguments");
				continue;
			}
			Class<?>[] params = method.getParameterTypes();
			if(params.length != 2) {
				System.out.println("Warning: Couldn't register event subscriber " + method.getName() + " of class " + eventListenerClass + ": Invalid number of arguments");
				continue;
			}
			if(!BaseEvent.class.isAssignableFrom(params[1])) {
				System.out.println("Warning: Couldn't register event subscriber " + method.getName() + " of class " + eventListenerClass + ": Invalid type of second argument");
				continue;
			}
			
			if(!subscribeEvent((Class<? extends BaseEvent>) params[1], method)) {
				System.out.println("Warning: Couldn't register event subscriber " + method.getName() + " of class " + eventListenerClass);
				continue;
			}
			
			atLeastOne = true;
		}
		return atLeastOne;
	}
	
	@SuppressWarnings("unchecked")
	public <T> boolean subscribeEventListener(T eventListener) {
		Method[] methods = ClassReflection.getMethods(eventListener.getClass());
		boolean atLeastOne = false;
		for(Method method : methods) {
			if(method.isStatic() || method.isAbstract()) continue;
			if(!method.isAnnotationPresent(SubscribeEvent.class)) continue;

			if(method.isVarArgs()) {
				System.out.println("Warning: Couldn't register event subscriber " + method.getName() + " of class " + eventListener.getClass() + ": Invalid number of arguments");
				continue;
			}
			Class<?>[] params = method.getParameterTypes();
			if(params.length != 2) {
				System.out.println("Warning: Couldn't register event subscriber " + method.getName() + " of class " + eventListener.getClass() + ": Invalid number of arguments");
				continue;
			}
			if(!BaseEvent.class.isAssignableFrom(params[1])) {
				System.out.println("Warning: Couldn't register event subscriber " + method.getName() + " of class " + eventListener.getClass() + ": Invalid type of second argument");
				continue;
			};
			
			if(!subscribeEvent((Class<? extends BaseEvent>) params[1], method, eventListener)) {
				System.out.println("Warning: Couldn't register event subscriber " + method.getName() + " of class " + eventListener.getClass());
				continue;
			}
			
			atLeastOne = true;
		}
		return atLeastOne;
	}

	public void dispatchEvent(BaseEvent event) {
		Array<BiConsumer<EventBus, BaseEvent>> consumers = subscribers.getOrDefault(event.getClass(), null);
		if(consumers == null) {
			return;
		}
		for(BiConsumer<EventBus, BaseEvent> consumer : consumers) {
			consumer.accept(this, event);
			if(!event.bubbles()) break;
		}
	}
}