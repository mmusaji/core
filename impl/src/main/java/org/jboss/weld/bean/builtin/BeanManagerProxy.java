/*
 * JBoss, Home of Professional Open Source
 * Copyright 2012, Red Hat, Inc., and individual contributors
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jboss.weld.bean.builtin;

import static org.jboss.weld.ContainerState.SHUTDOWN;

import java.io.ObjectStreamException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Set;

import javax.enterprise.context.spi.Contextual;
import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.Instance;
import javax.enterprise.inject.spi.AnnotatedType;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.Decorator;
import javax.enterprise.inject.spi.InjectionPoint;
import javax.enterprise.inject.spi.InjectionTarget;
import javax.enterprise.inject.spi.InterceptionType;
import javax.enterprise.inject.spi.Interceptor;
import javax.enterprise.inject.spi.ObserverMethod;

import org.jboss.weld.Container;
import org.jboss.weld.ContainerState;
import org.jboss.weld.SystemPropertiesConfiguration;
import org.jboss.weld.bootstrap.api.ServiceRegistry;
import org.jboss.weld.bootstrap.spi.BootstrapConfiguration;
import org.jboss.weld.construction.api.WeldCreationalContext;
import org.jboss.weld.ejb.spi.EjbDescriptor;
import org.jboss.weld.exceptions.IllegalStateException;
import org.jboss.weld.logging.BeanManagerLogger;
import org.jboss.weld.manager.BeanManagerImpl;
import org.jboss.weld.manager.api.WeldInjectionTargetBuilder;
import org.jboss.weld.manager.api.WeldInjectionTargetFactory;
import org.jboss.weld.manager.api.WeldManager;
import org.jboss.weld.serialization.spi.BeanIdentifier;
import org.jboss.weld.util.ForwardingBeanManager;
import org.jboss.weld.util.reflection.Reflections;

/**
 * Client view of {@link BeanManagerImpl}.
 *
 * @author Martin Kouba
 */
public class BeanManagerProxy extends ForwardingBeanManager implements WeldManager {

    private static final String GET_BEANS_METHOD_NAME = "getBeans()";

    private static final long serialVersionUID = -6990849486568169846L;

    private final BeanManagerImpl manager;
    private transient volatile Container container;
    private final boolean nonPortableMode;

    public BeanManagerProxy(BeanManagerImpl manager) {
        this.manager = manager;
        if (SystemPropertiesConfiguration.INSTANCE.isNonPortableModeEnabled()) {
            this.nonPortableMode = true;
        } else {
            this.nonPortableMode = manager.getServices().get(BootstrapConfiguration.class).isNonPortableModeEnabled();
        }
    }

    @Override
    public BeanManagerImpl delegate() {
        return manager;
    }

    @Override
    public Object getReference(Bean<?> bean, Type beanType, CreationalContext<?> ctx) {
        checkContainerState("getReference()", ContainerState.VALIDATED);
        return super.getReference(bean, beanType, ctx);
    }

    @Override
    public Object getInjectableReference(InjectionPoint ij, CreationalContext<?> ctx) {
        checkContainerState("getInjectableReference()", ContainerState.VALIDATED);
        return super.getInjectableReference(ij, ctx);
    }

    @Override
    public Set<Bean<?>> getBeans(Type beanType, Annotation... qualifiers) {
        checkContainerState(GET_BEANS_METHOD_NAME);
        return super.getBeans(beanType, qualifiers);
    }

    @Override
    public Set<Bean<?>> getBeans(String name) {
        checkContainerState(GET_BEANS_METHOD_NAME);
        return super.getBeans(name);
    }

    @Override
    public Bean<?> getPassivationCapableBean(String id) {
        checkContainerState("getPassivationCapableBean()");
        return super.getPassivationCapableBean(id);
    }

    @Override
    public <X> Bean<? extends X> resolve(Set<Bean<? extends X>> beans) {
        checkContainerState("resolve()");
        return super.resolve(beans);
    }

    @Override
    public void validate(InjectionPoint injectionPoint) {
        checkContainerState("validate()");
        super.validate(injectionPoint);
    }

    @Override
    public <T> Set<ObserverMethod<? super T>> resolveObserverMethods(T event, Annotation... qualifiers) {
        checkContainerState("resolveObserverMethods()");
        return super.resolveObserverMethods(event, qualifiers);
    }

    @Override
    public List<Decorator<?>> resolveDecorators(Set<Type> types, Annotation... qualifiers) {
        checkContainerState("resolveDecorators()");
        return super.resolveDecorators(types, qualifiers);
    }

    @Override
    public List<Interceptor<?>> resolveInterceptors(InterceptionType type, Annotation... interceptorBindings) {
        checkContainerState("resolveInterceptors()");
        return super.resolveInterceptors(type, interceptorBindings);
    }

    @Override
    public WeldManager createActivity() {
        return delegate().createActivity();
    }

    @Override
    public WeldManager setCurrent(Class<? extends Annotation> scopeType) {
        return delegate().setCurrent(scopeType);
    }

    @Override
    public <T> InjectionTarget<T> createInjectionTarget(EjbDescriptor<T> descriptor) {
        return delegate().createInjectionTarget(descriptor);
    }

    @Override
    public <T> Bean<T> getBean(EjbDescriptor<T> descriptor) {
        return delegate().getBean(descriptor);
    }

    @Override
    public <T> EjbDescriptor<T> getEjbDescriptor(String ejbName) {
        return delegate().getEjbDescriptor(ejbName);
    }

    @Override
    public ServiceRegistry getServices() {
        return delegate().getServices();
    }

    @Override
    public WeldManager getCurrent() {
        return delegate().getCurrent();
    }

    @Override
    public <X> InjectionTarget<X> fireProcessInjectionTarget(AnnotatedType<X> type) {
        return delegate().fireProcessInjectionTarget(type);
    }

    @Override
    public <X> InjectionTarget<X> fireProcessInjectionTarget(AnnotatedType<X> annotatedType, InjectionTarget<X> injectionTarget) {
        return delegate().fireProcessInjectionTarget(annotatedType, injectionTarget);
    }

    @Override
    public String getId() {
        return delegate().getId();
    }

    @Override
    public Instance<Object> instance() {
        return delegate().instance();
    }

    @Override
    public Bean<?> getPassivationCapableBean(BeanIdentifier identifier) {
        return delegate().getPassivationCapableBean(identifier);
    }

    @Override
    public <T> WeldInjectionTargetBuilder<T> createInjectionTargetBuilder(AnnotatedType<T> type) {
        return delegate().createInjectionTargetBuilder(type);
    }

    @Override
    public <T> WeldInjectionTargetFactory<T> getInjectionTargetFactory(AnnotatedType<T> annotatedType) {
        return delegate().getInjectionTargetFactory(annotatedType);
    }

    @Override
    public <T> WeldCreationalContext<T> createCreationalContext(Contextual<T> contextual) {
        return delegate().createCreationalContext(contextual);
    }

    protected Object readResolve() throws ObjectStreamException {
        return new BeanManagerProxy(this.manager);
    }

    /**
     * When in portable mode (default) this method verifies that the container has reached the specified minimal state.
     * If it hasn't, an {@link IllegalStateException} is thrown. When in non-portable mode this method is no-op.
     *
     * @param methodName
     * @param minimalState the minimal state
     * @throws IllegalStateException If the application initialization is not finished yet
     */
    private void checkContainerState(String methodName, ContainerState minimalState) {
        if (nonPortableMode) {
            return;
        }
        if (this.container == null) {
            this.container = Container.instance(manager);
        }

        ContainerState state = container.getState();
        if (SHUTDOWN.equals(state)) {
            throw BeanManagerLogger.LOG.methodNotAvailableAfterShutdown(methodName);
        }
        if (state.compareTo(minimalState) < 0) {
            throw BeanManagerLogger.LOG.methodNotAvailableDuringInitialization(methodName);
        }
    }

    private void checkContainerState(String methodName) {
        checkContainerState(methodName, ContainerState.DISCOVERED);
    }

    public static BeanManagerImpl unwrap(BeanManager manager) {
        if (manager instanceof ForwardingBeanManager) {
            manager = Reflections.<ForwardingBeanManager> cast(manager).delegate();
        }
        if (manager instanceof BeanManagerImpl) {
            return (BeanManagerImpl) manager;
        }
        throw new IllegalArgumentException("Unknown BeanManager " + manager);
    }

    @Override
    public BeanManagerImpl unwrap() {
        return delegate();
    }

}
