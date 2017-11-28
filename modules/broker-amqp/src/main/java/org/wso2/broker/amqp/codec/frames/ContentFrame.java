/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
 */

package org.wso2.broker.amqp.codec.frames;

import io.netty.buffer.ByteBuf;

/**
 * AMQP content frame
 */
public class ContentFrame extends GeneralFrame {
    private final long length;
    private final ByteBuf payload;

    public ContentFrame(int channel, long length, ByteBuf payload) {
        super((byte) 3, channel);
        this.length = length;
        this.payload = payload;
    }

    @Override
    public long getPayloadSize() {
        return 4L + length;
    }

    @Override
    public void writePayload(ByteBuf buf) {
        buf.writeInt((int) length);
        buf.writeBytes(payload);
    }

    public static ContentFrame parse(ByteBuf buf, int channel, long payloadSize) {
        ByteBuf payload = buf.retainedSlice(buf.readerIndex(), (int) payloadSize);
        buf.skipBytes((int) payloadSize);

        return new ContentFrame(channel, payloadSize, payload);
    }
}
