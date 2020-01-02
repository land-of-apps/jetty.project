//
//  ========================================================================
//  Copyright (c) 1995-2020 Mort Bay Consulting Pty. Ltd.
//  ------------------------------------------------------------------------
//  All rights reserved. This program and the accompanying materials
//  are made available under the terms of the Eclipse Public License v1.0
//  and Apache License v2.0 which accompanies this distribution.
//
//      The Eclipse Public License is available at
//      http://www.eclipse.org/legal/epl-v10.html
//
//      The Apache License v2.0 is available at
//      http://www.opensource.org/licenses/apache2.0.php
//
//  You may elect to redistribute this code under either of these licenses.
//  ========================================================================
//

package org.eclipse.jetty.plus.annotation;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CopyOnWriteArraySet;

import org.eclipse.jetty.util.log.Log;
import org.eclipse.jetty.util.log.Logger;

/**
 * LifeCycleCallbackCollection
 * 
 * This class collects the classes and methods that have been configured
 * in web.xml with postconstruct/predestroy callbacks, or that contain the
 * equivalent annotations.  It is also responsible for calling the 
 * callbacks.
 * 
 * This class is not threadsafe for concurrent modifications, but is
 * threadsafe for reading with concurrent modifications.
 */
public class LifeCycleCallbackCollection
{
    private static final Logger LOG = Log.getLogger(LifeCycleCallbackCollection.class);

    public static final String LIFECYCLE_CALLBACK_COLLECTION = "org.eclipse.jetty.lifecyleCallbackCollection";

    private final ConcurrentMap<String, Set<LifeCycleCallback>> postConstructCallbacksMap = new ConcurrentHashMap<>();
    private final ConcurrentMap<String, Set<LifeCycleCallback>> preDestroyCallbacksMap = new ConcurrentHashMap<>();

    /**
     * Add a Callback to the list of callbacks.
     *
     * @param callback the callback
     */
    public void add(LifeCycleCallback callback)
    {
        if (callback == null)
        {
            if (LOG.isDebugEnabled())
                LOG.debug("Ignoring empty LifeCycleCallback");
            return;
        }

        Map<String, Set<LifeCycleCallback>> map = null;
        if (callback instanceof PreDestroyCallback)
            map = preDestroyCallbacksMap;
        else if (callback instanceof PostConstructCallback)
            map = postConstructCallbacksMap;
        else
            throw new IllegalArgumentException("Unsupported lifecycle callback type: " + callback);

        Set<LifeCycleCallback> callbacks = map.get(callback.getTargetClassName());
        if (callbacks == null)
        {
            callbacks = new CopyOnWriteArraySet<LifeCycleCallback>();
            Set<LifeCycleCallback> tmp = map.putIfAbsent(callback.getTargetClassName(), callbacks);
            if (tmp != null)
                callbacks = tmp;
        }

        boolean added = callbacks.add(callback);
        if (LOG.isDebugEnabled())
            LOG.debug("Adding callback for class={} on method={} added={}", callback.getTargetClassName(), callback.getMethodName(), added);
    }

    public Set<LifeCycleCallback> getPreDestroyCallbacks(Object o)
    {
        if (o == null)
            return null;

        Class<? extends Object> clazz = o.getClass();
        return preDestroyCallbacksMap.get(clazz.getName());
    }

    public Set<LifeCycleCallback> getPostConstructCallbacks(Object o)
    {
        if (o == null)
            return null;

        Class<? extends Object> clazz = o.getClass();
        return postConstructCallbacksMap.get(clazz.getName());
    }

    /**
     * Call the method, if one exists, that is annotated with <code>&#064;PostConstruct</code>
     * or with <code>&lt;post-construct&gt;</code> in web.xml
     *
     * @param o the object on which to attempt the callback
     * @throws Exception if unable to call {@link PostConstructCallback}
     */
    public void callPostConstructCallback(Object o)
        throws Exception
    {
        if (o == null)
            return;

        Class<? extends Object> clazz = o.getClass();
        Set<LifeCycleCallback> callbacks = postConstructCallbacksMap.get(clazz.getName());

        if (callbacks == null)
            return;

        for (LifeCycleCallback l : callbacks)
            l.callback(o);
    }

    /**
     * Call the method, if one exists, that is annotated with <code>&#064;PreDestroy</code>
     * or with <code>&lt;pre-destroy&gt;</code> in web.xml
     *
     * @param o the object on which to attempt the callback
     * @throws Exception if unable to call {@link PreDestroyCallback}
     */
    public void callPreDestroyCallback(Object o)
        throws Exception
    {
        if (o == null)
            return;

        Class<? extends Object> clazz = o.getClass();
        Set<LifeCycleCallback> callbacks = preDestroyCallbacksMap.get(clazz.getName());

        if (callbacks == null)
            return;

        for (LifeCycleCallback l : callbacks)
            l.callback(o);
    }

    /**
     * Generate a read-only view of the post-construct callbacks
     *
     * @return the map of {@link PostConstructCallback}s
     */
    public Map<String, Set<LifeCycleCallback>> getPostConstructCallbackMap()
    {
        return Collections.unmodifiableMap(postConstructCallbacksMap);
    }

    /**
     * Generate a read-only view of the pre-destroy callbacks
     *
     * @return the map of {@link PreDestroyCallback}s
     */
    public Map<String, Set<LifeCycleCallback>> getPreDestroyCallbackMap()
    {
        return Collections.unmodifiableMap(preDestroyCallbacksMap);
    }

    /**
     * Amalgamate all post-construct callbacks and return a read only set
     *
     * @return the collection of {@link PostConstructCallback}s
     */
    public Collection<LifeCycleCallback> getPostConstructCallbacks()
    {
        Set<LifeCycleCallback> set = new HashSet<LifeCycleCallback>();
        for (String s : postConstructCallbacksMap.keySet())
        {
            set.addAll(postConstructCallbacksMap.get(s));
        }
        return Collections.unmodifiableCollection(set);
    }

    /**
     * Amalgamate all pre-destroy callbacks and return a read only set
     *
     * @return the collection of {@link PreDestroyCallback}s
     */
    public Collection<LifeCycleCallback> getPreDestroyCallbacks()
    {
        Set<LifeCycleCallback> set = new HashSet<LifeCycleCallback>();
        for (String s : preDestroyCallbacksMap.keySet())
        {
            set.addAll(preDestroyCallbacksMap.get(s));
        }
        return Collections.unmodifiableCollection(set);
    }
}
