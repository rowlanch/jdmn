/**
 * Copyright 2016 Goldman Sachs.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 *
 * You may obtain a copy of the License at
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations under the License.
 */
package com.gs.dmn.dialect;

import com.gs.dmn.DMNModelRepository;
import com.gs.dmn.feel.analysis.semantics.environment.DefaultDMNEnvironmentFactory;
import com.gs.dmn.feel.analysis.semantics.environment.EnvironmentFactory;
import com.gs.dmn.feel.lib.DefaultFEELLib;
import com.gs.dmn.feel.lib.FEELLib;
import com.gs.dmn.feel.synthesis.type.FEELTypeTranslator;
import com.gs.dmn.feel.synthesis.type.StandardFEELTypeTranslator;
import com.gs.dmn.log.BuildLogger;
import com.gs.dmn.runtime.DefaultDMNBaseDecision;
import com.gs.dmn.runtime.interpreter.DMNInterpreter;
import com.gs.dmn.serialization.DMNValidator;
import com.gs.dmn.serialization.StandardDMNValidator;
import com.gs.dmn.transformation.DMNToJavaTransformer;
import com.gs.dmn.transformation.DMNTransformer;
import com.gs.dmn.transformation.basic.BasicDMN2JavaTransformer;
import com.gs.dmn.transformation.template.TreeTemplateProvider;
import org.omg.spec.dmn._20151101.dmn.TDefinitions;

import java.util.Map;

public class StandardDMNDialectDefinition extends AbstractDMNDialectDefinition {
    //
    // DMN Processors
    //
    @Override
    public DMNInterpreter createDMNInterpreter(TDefinitions definitions) {
        return new DMNInterpreter(createBasicTransformer(definitions, null), createFEELLib());
    }

    @Override
    public DMNToJavaTransformer createDMNToJavaTransformer(DMNTransformer dmnTransformer, Map<String, String> inputParameters, BuildLogger logger) {
        return new DMNToJavaTransformer(this, dmnTransformer, inputParameters, logger, new TreeTemplateProvider());
    }

    @Override
    public BasicDMN2JavaTransformer createBasicTransformer(TDefinitions definitions, String javaRootPackage) {
        EnvironmentFactory environmentFactory = createEnvironmentFactory();
        return new BasicDMN2JavaTransformer(createModelRepository(definitions), environmentFactory, createTypeTranslator(), javaRootPackage);
    }

    private DMNModelRepository createModelRepository(TDefinitions definitions) {
        if (definitions == null) {
            return new DMNModelRepository();
        } else {
            return new DMNModelRepository(definitions);
        }
    }

    //
    // DMN execution
    //
    @Override
    public FEELTypeTranslator createTypeTranslator() {
        return new StandardFEELTypeTranslator();
    }

    @Override
    public FEELLib createFEELLib() {
        return new DefaultFEELLib();
    }

    @Override
    public String getDecisionBaseClass() {
        return DefaultDMNBaseDecision.class.getName();
    }

    @Override
    public DMNValidator createValidator(boolean semanticValidation) {
        return new StandardDMNValidator(semanticValidation);
    }

    private EnvironmentFactory createEnvironmentFactory() {
        return DefaultDMNEnvironmentFactory.instance();
    }
}
