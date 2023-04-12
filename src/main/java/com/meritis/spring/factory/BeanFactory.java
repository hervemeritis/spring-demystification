package com.meritis.spring.factory;

import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;
import org.reflections.Reflections;

import javax.inject.Inject;
import javax.inject.Named;
import java.lang.reflect.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;

public class BeanFactory {
    private final Map<Class<?>, Object> singletons = new ConcurrentHashMap<>();

    public void load(String packageName) {
        new Reflections(packageName).getTypesAnnotatedWith(Named.class)
                                    .stream()
                                    .filter(Predicate.not(singletons::containsKey))
                                    .forEach(this::createSingleton);
    }

    public <T> T getSingleton(Class<T> aClass) {
        return (T) singletons.get(aClass);
    }

    private Object createSingleton(Class<?> aClass) {
        Constructor<?> constructorToInvoke = Arrays.stream(aClass.getConstructors())
                                                   .filter(constructor -> constructor.isAnnotationPresent(Inject.class))
                                                   .findFirst()
                                                   .orElseThrow();
        List<Object> parameterToInject = new ArrayList<>();
        for (Class<?> aConstructorParameter : constructorToInvoke.getParameterTypes()) {
            Optional.ofNullable(singletons.get(aConstructorParameter))
                    .ifPresentOrElse(parameterToInject::add, () -> parameterToInject.add(createSingleton(aConstructorParameter)));
        }
        Object proxiedObject = createProxy(aClass, constructorToInvoke, parameterToInject);
        singletons.put(aClass, proxiedObject);
        return proxiedObject;
    }

    private Object createProxy(Class<?> aClass, Constructor<?> constructorToInvoke, List<Object> parameterToInject) {
        Enhancer enhancer = new Enhancer();
        enhancer.setSuperclass(aClass);
        try {
            Object target = constructorToInvoke.newInstance(parameterToInject.toArray());
            enhancer.setCallback(new ClassProxy(target));
            return enhancer.create(constructorToInvoke.getParameterTypes(), parameterToInject.toArray());
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }

    }

    private static class ClassProxy implements MethodInterceptor {
        private final Object target;

        private ClassProxy(Object target) {
            this.target = target;
        }


        @Override
        public Object intercept(Object o, Method method, Object[] objects, MethodProxy methodProxy) throws Throwable {
            return method.invoke(target, objects);
        }
    }
}
