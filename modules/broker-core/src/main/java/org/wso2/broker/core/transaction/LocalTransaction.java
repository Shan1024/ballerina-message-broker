/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.broker.core.transaction;

import org.wso2.broker.common.ValidationException;
import org.wso2.broker.core.BrokerException;
import org.wso2.broker.core.Message;

public class LocalTransaction {

    public LocalTransaction(Branch branch) {
    }

    public void enqueue(Message message) {
    }

    public void dequeue(String queueName, Message message) {
    }

    public void commit() throws BrokerException, ValidationException {
    }

    public void rollback() throws BrokerException, ValidationException {
    }
}
