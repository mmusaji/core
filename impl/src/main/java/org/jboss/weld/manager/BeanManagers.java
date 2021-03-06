/*
 * JBoss, Home of Professional Open Source
 * Copyright 2010, Red Hat, Inc., and individual contributors
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
package org.jboss.weld.manager;

import java.util.Comparator;
import java.util.HashSet;
import java.util.Set;

import org.jboss.weld.manager.api.WeldManager;


public class BeanManagers {

    public static final Comparator<WeldManager> ID_COMPARATOR = new Comparator<WeldManager>() {
        @Override
        public int compare(WeldManager m1, WeldManager m2) {
            return m1.getId().compareTo(m2.getId());
        }
    };

    private BeanManagers() {
    }

    public static <T> Set<Iterable<T>> getDirectlyAccessibleComponents(BeanManagerImpl beanManager, Transform<T> transform) {
        Set<Iterable<T>> result = new HashSet<Iterable<T>>();
        result.add(transform.transform(beanManager));
        for (BeanManagerImpl accessibleBeanManager : beanManager.getAccessibleManagers()) {
            result.add(transform.transform(accessibleBeanManager));
        }
        return result;
    }
}
