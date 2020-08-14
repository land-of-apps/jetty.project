//
// ========================================================================
// Copyright (c) 1995-2020 Mort Bay Consulting Pty Ltd and others.
//
// This program and the accompanying materials are made available under
// the terms of the Eclipse Public License 2.0 which is available at
// https://www.eclipse.org/legal/epl-2.0
//
// This Source Code may also be made available under the following
// Secondary Licenses when the conditions for such availability set
// forth in the Eclipse Public License, v. 2.0 are satisfied:
// the Apache License v2.0 which is available at
// https://www.apache.org/licenses/LICENSE-2.0
//
// SPDX-License-Identifier: EPL-2.0 OR Apache-2.0
// ========================================================================
//

package org.eclipse.jetty.websocket.javax.common.encoders;

import java.nio.ByteBuffer;
import javax.websocket.EncodeException;
import javax.websocket.Encoder;
import javax.websocket.EndpointConfig;

public class ByteArrayEncoder implements Encoder.Binary<byte[]>
{
    @Override
    public void destroy()
    {
        /* do nothing */
    }

    @Override
    public ByteBuffer encode(byte[] object) throws EncodeException
    {
        return ByteBuffer.wrap(object);
    }

    @Override
    public void init(EndpointConfig config)
    {
        /* do nothing */
    }
}
