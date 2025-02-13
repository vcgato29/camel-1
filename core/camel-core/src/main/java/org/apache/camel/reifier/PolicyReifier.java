/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.camel.reifier;

import org.apache.camel.Processor;
import org.apache.camel.Service;
import org.apache.camel.model.PolicyDefinition;
import org.apache.camel.model.ProcessorDefinition;
import org.apache.camel.processor.WrapProcessor;
import org.apache.camel.spi.Policy;
import org.apache.camel.spi.RouteContext;
import org.apache.camel.util.ObjectHelper;

public class PolicyReifier extends ProcessorReifier<PolicyDefinition> {

    PolicyReifier(ProcessorDefinition<?> definition) {
        super((PolicyDefinition) definition);
    }

    @Override
    public Processor createProcessor(RouteContext routeContext) throws Exception {
        Policy policy = resolvePolicy(routeContext);
        ObjectHelper.notNull(policy, "policy", definition);

        // before wrap
        policy.beforeWrap(routeContext, definition);

        // create processor after the before wrap
        Processor childProcessor = this.createChildProcessor(routeContext, true);

        // wrap
        Processor target = policy.wrap(routeContext, childProcessor);

        if (!(target instanceof Service)) {
            // wrap the target so it becomes a service and we can manage its lifecycle
            target = new WrapProcessor(target, childProcessor);
        }
        return target;
    }

    protected Policy resolvePolicy(RouteContext routeContext) {
        if (definition.getPolicy() != null) {
            return definition.getPolicy();
        }
        // reuse code on transacted definition to do the resolution
        return TransactedReifier.resolvePolicy(routeContext, definition.getRef(), definition.getType());
    }

}
