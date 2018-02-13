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
package com.gs.dmn.runtime.compiler;

import com.gs.dmn.dialect.DMNDialectDefinition;
import com.gs.dmn.dialect.StandardDMNDialectDefinition;
import com.gs.dmn.feel.analysis.semantics.type.BuiltinFunctionType;
import com.gs.dmn.feel.analysis.semantics.type.StringType;
import com.gs.dmn.feel.analysis.syntax.ast.FEELContext;
import com.gs.dmn.feel.analysis.syntax.ast.expression.function.FunctionDefinition;
import com.gs.dmn.feel.analysis.syntax.ast.expression.literal.NumericLiteral;
import com.gs.dmn.feel.synthesis.FEELTranslator;
import com.gs.dmn.transformation.basic.BasicDMN2JavaTransformer;
import org.junit.Test;
import org.omg.spec.dmn._20151101.dmn.TDefinitions;

import java.util.Arrays;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * Created by Octavian Patrascoiu on 02/11/2017.
 */
public class JavaToolsCompilerTest extends AbstractCompilerTest {
    @Test
    public void testMakeClassData() throws Exception {
        ClassData classData = makeClassData();

        assertEquals(JavaxToolsClassData.class.getName(), classData.getClass().getName());
        assertEquals("com.gs.dmn.runtime", classData.getPackageName());
        assertEquals("LambdaExpressionImpl", classData.getClassName());
        assertNotNull("", ((JavaxToolsClassData)classData).getClassText());
    }

    @Test
    public void testCompile() throws Exception {
        Class<?> cls = getCompiler().compile(makeClassData());
        assertNotNull(cls);
    }

    @Override
    protected JavaCompiler getCompiler() {
        return new JavaxToolsCompiler();
    }
}