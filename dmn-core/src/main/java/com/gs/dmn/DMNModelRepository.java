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
package com.gs.dmn;

import com.gs.dmn.runtime.DMNRuntimeException;
import com.gs.dmn.serialization.DMNNamespacePrefixMapper;
import com.gs.dmn.transformation.DMNToJavaTransformer;
import org.apache.commons.lang3.StringUtils;
import org.omg.spec.dmn._20151101.dmn.*;

import javax.xml.bind.JAXBElement;
import javax.xml.namespace.QName;
import java.util.*;

/**
 * Created by Octavian Patrascoiu on 09/05/2017.
 */
public class DMNModelRepository {
    public static final ObjectFactory OBJECT_FACTORY = new ObjectFactory();

    private final TDefinitions definitions;

    public DMNModelRepository() {
        this(OBJECT_FACTORY.createTDefinitions());
    }

    public DMNModelRepository(TDefinitions definitions) {
        this.definitions = definitions;
        normalize(definitions);
    }

    private void normalize(TDefinitions definitions) {
        if (definitions != null) {
            sortDRGElements(definitions.getDrgElement());
            sortNamedElements(definitions.getItemDefinition());
        }
    }

    public String removeSingleQuotes(String name) {
        if (isQuotedName(name)) {
            name = name.substring(1, name.length() - 1);
        }
        return name;
    }

    private boolean isQuotedName(String name) {
        return name != null && name.startsWith("'") && name.endsWith("'");
    }

    public TDefinitions getDefinitions() {
        return definitions;
    }

    public List<TDRGElement> drgElements() {
        List<TDRGElement> result = new ArrayList<>();
        for (JAXBElement<? extends TDRGElement> jaxbElement : definitions.getDrgElement()) {
            TDRGElement element = jaxbElement.getValue();
            result.add(element);
        }
        return result;
    }

    public List<TDecision> decisions() {
        List<TDecision> result = new ArrayList<>();
        for (JAXBElement<? extends TDRGElement> jaxbElement : definitions.getDrgElement()) {
            TDRGElement element = jaxbElement.getValue();
            if (element instanceof TDecision) {
                result.add((TDecision) element);
            }
        }
        return result;
    }

    public List<TInputData> inputDatas() {
        List<TInputData> result = new ArrayList<>();
        for (JAXBElement<? extends TDRGElement> jaxbElement : definitions.getDrgElement()) {
            TDRGElement element = jaxbElement.getValue();
            if (element instanceof TInputData) {
                result.add((TInputData) element);
            }
        }
        return result;
    }

    public List<TBusinessKnowledgeModel> businessKnowledgeModels() {
        List<TBusinessKnowledgeModel> result = new ArrayList<>();
        for (JAXBElement<? extends TDRGElement> jaxbElement : definitions.getDrgElement()) {
            TDRGElement element = jaxbElement.getValue();
            if (element instanceof TBusinessKnowledgeModel) {
                result.add((TBusinessKnowledgeModel) element);
            }
        }
        return result;
    }

    public List<TItemDefinition> itemDefinitions() {
        List<TItemDefinition> itemDefinition = definitions.getItemDefinition();
        return itemDefinition;
    }

    public List<TItemDefinition> sortItemComponent(TItemDefinition itemDefinition) {
        if (itemDefinition == null || itemDefinition.getItemComponent() == null) {
            return null;
        }
        List<TItemDefinition> children = new ArrayList<>(itemDefinition.getItemComponent());
        children.sort(new Comparator<TItemDefinition>() {
            @Override
            public int compare(TItemDefinition o1, TItemDefinition o2) {
                if (o1 == null && o2 == null) {
                    return 0;
                } if (o1 == null && o2 != null) {
                    return 1;
                } if (o1 != null && o2 == null) {
                    return -1;
                } else {
                    return o1.getName().compareTo(o2.getName());
                }
            }
        });
        return children;
    }

    public TItemDefinition normalize(TItemDefinition itemDefinition) {
        while (true) {
            TItemDefinition next = next(itemDefinition);
            if (next != null) {
                itemDefinition = next;
            } else {
                break;
            }
        }
        return itemDefinition;
    }

    private TItemDefinition next(TItemDefinition itemDefinition) {
        if (itemDefinition == null
                || itemDefinition.isIsCollection()
                || !isEmpty(itemDefinition.getItemComponent())
                || itemDefinition.getTypeRef() == null) {
            return null;
        }
        return lookupItemDefinition(itemDefinition.getTypeRef());
    }

    private void sortDRGElements(List<JAXBElement<? extends TDRGElement>> result) {
        result.sort(Comparator.comparing((JAXBElement<? extends TDRGElement> o) -> removeSingleQuotes(o.getValue().getName())));
    }

    public void sortNamedElements(List<? extends TNamedElement> result) {
        result.sort(Comparator.comparing((TNamedElement o) -> removeSingleQuotes(o.getName())));
    }

    public TDecision findDecisionById(String href) {
        for (TDecision decision : decisions()) {
            if (sameId(decision, href)) {
                return decision;
            }
        }
        throw new DMNRuntimeException(String.format("Cannot find decision for href='%s'", href));
    }

    public TInputData findInputDataById(String href) {
        for (TInputData inputData : inputDatas()) {
            if (sameId(inputData, href)) {
                return inputData;
            }
        }
        throw new DMNRuntimeException(String.format("Cannot find input data for href='%s'", href));
    }

    public TBusinessKnowledgeModel findKnowledgeModelById(String href) {
        for (TBusinessKnowledgeModel knowledgeModel : businessKnowledgeModels()) {
            if (sameId(knowledgeModel, href)) {
                return knowledgeModel;
            }
        }
        throw new DMNRuntimeException(String.format("Cannot find business knowledge model for href='%s'", href));
    }

    public TBusinessKnowledgeModel findKnowledgeModelByName(String name) {
        for (TBusinessKnowledgeModel knowledgeModel : businessKnowledgeModels()) {
            if (sameName(knowledgeModel, name)) {
                return knowledgeModel;
            }
        }
        throw new DMNRuntimeException(String.format("Cannot find business knowledge model for href='%s'", name));
    }

    public TDRGElement findDRGElementByName(String href) {
        for (TDRGElement element : drgElements()) {
            if (sameName(element, href)) {
                return element;
            }
        }
        throw new DMNRuntimeException(String.format("Cannot find element for href='%s'", href));
    }

    public boolean sameId(TDMNElement element, String href) {
        if (href.startsWith("#")) {
            href = href.substring(1);
        }
        return element.getId().equals(href);
    }

    private boolean sameName(TNamedElement element, String href) {
        return element.getName().equals(href);
    }

    public List<TDecision> directSubDecisions(TDRGElement element) {
        List<TDecision> decisions = new ArrayList<>();
        if (element instanceof TDecision) {
            for (TInformationRequirement ir : ((TDecision) element).getInformationRequirement()) {
                TDMNElementReference requiredDecision = ir.getRequiredDecision();
                if (requiredDecision != null) {
                    decisions.add(findDecisionById(requiredDecision.getHref()));
                }
            }
            sortNamedElements(decisions);
        }
        return decisions;
    }

    public Collection<TDecision> allSubDecisions(TDRGElement element) {
        Set<TDecision> decisions = new LinkedHashSet<>();
        collectSubDecisions(element, decisions);
        return decisions;
    }

    private void collectSubDecisions(TDRGElement element, Collection<TDecision> decisions) {
        decisions.addAll(directSubDecisions(element));
        for (TDecision child : directSubDecisions(element)) {
            collectSubDecisions(child, decisions);
        }
    }

    public List<TInputData> directInputDatas(TDRGElement element) {
        if (element instanceof TDRGElement) {
            List<TInformationRequirement> informationRequirement = ((TDecision) element).getInformationRequirement();
            return directInputDatas(informationRequirement);
        } else {
            return new ArrayList<>();
        }
    }

    private List<TInputData> directInputDatas(List<TInformationRequirement> informationRequirement) {
        List<TInputData> result = new ArrayList<>();
        for (TInformationRequirement ir : informationRequirement) {
            TDMNElementReference requiredInput = ir.getRequiredInput();
            if (requiredInput != null) {
                TInputData inputData = findInputDataById(requiredInput.getHref());
                if (inputData != null) {
                    result.add(inputData);
                } else {
                    throw new DMNRuntimeException(String.format("Cannot find InputData for '%s'", requiredInput.getHref()));
                }
            }
        }
        return result;
    }

    public List<TInputData> allInputDatas(TDRGElement element) {
        if (element instanceof TDRGElement) {
            Set<TInputData> result = new LinkedHashSet<>();
            collectInputDatas(element, result);
            return new ArrayList<>(result);
        } else {
            return new ArrayList<>();
        }
    }

    protected void collectInputDatas(TDRGElement element, Set<TInputData> inputDatas) {
        inputDatas.addAll(directInputDatas(element));
        for (TDecision child : directSubDecisions(element)) {
            collectInputDatas(child, inputDatas);
        }
    }

    public List<TBusinessKnowledgeModel> directSubBKMs(TDRGElement element) {
        List<TBusinessKnowledgeModel> result = new ArrayList<>();
        List<TKnowledgeRequirement> knowledgeRequirements;
        if (element instanceof TDecision) {
            knowledgeRequirements = ((TDecision) element).getKnowledgeRequirement();
        } else if (element instanceof TBusinessKnowledgeModel) {
            knowledgeRequirements = ((TBusinessKnowledgeModel) element).getKnowledgeRequirement();
        } else {
            knowledgeRequirements = new ArrayList<>();
        }
        for (TKnowledgeRequirement kr : knowledgeRequirements) {
            TDMNElementReference requiredInput = kr.getRequiredKnowledge();
            if (requiredInput != null) {
                TBusinessKnowledgeModel knowledgeModel = findKnowledgeModelById(requiredInput.getHref());
                if (knowledgeModel != null) {
                    result.add(knowledgeModel);
                } else {
                    throw new DMNRuntimeException(String.format("Cannot find BusinessKnowledgeModel for '%s'", requiredInput.getHref()));
                }
            }
        }
        return result;
    }

    public List<TBusinessKnowledgeModel> allKnowledgeModels(TDRGElement element) {
        Set<TBusinessKnowledgeModel> result = new LinkedHashSet<>();
        collectKnowledgeModels(element, result);
        return new ArrayList<>(result);
    }

    private void collectKnowledgeModels(TDRGElement element, Set<TBusinessKnowledgeModel> accumulator) {
        accumulator.addAll(directSubBKMs(element));
        for (TDRGElement child : directSubBKMs(element)) {
            collectKnowledgeModels(child, accumulator);
        }
    }

    public TDecisionTable decisionTable(TDRGElement element) {
        TExpression expression = expression(element);
        if (expression instanceof TDecisionTable) {
            return (TDecisionTable) expression;
        } else {
            throw new DMNRuntimeException(String.format("Cannot find decision table in element '%s'", element.getName()));
        }
    }

    public TItemDefinition lookupItemDefinition(QName typeRef) {
        return lookupItemDefinition(definitions.getItemDefinition(), typeRef);
    }

    TItemDefinition lookupItemDefinition(List<TItemDefinition> itemDefinitionList, QName typeRef) {
        if (typeRef == null || DMNNamespacePrefixMapper.FEEL_NS.equals(typeRef.getNamespaceURI())) {
            return null;
        }
        for (TItemDefinition itemDefinition : itemDefinitionList) {
            if (typeRef.getLocalPart().equals(itemDefinition.getName())) {
                return itemDefinition;
            }
        }
        return null;
    }

    protected TItemDefinition lookupItemDefinition(List<TItemDefinition> itemDefinitionList, String name) {
        if (name == null) {
            return null;
        }
        for (TItemDefinition itemDefinition : itemDefinitionList) {
            if (name.equals(itemDefinition.getName())) {
                return itemDefinition;
            }
        }
        return null;
    }

    public void collectInputs(TDecision decision, Set<TDRGElement> tdrgElements) {
        tdrgElements.addAll(directInputDatas(decision));
        tdrgElements.addAll(directSubDecisions(decision));
    }

    public boolean hasDefaultValue(TDecisionTable decisionTable) {
        List<TOutputClause> outputClauses = decisionTable.getOutput();
        for(TOutputClause output: outputClauses) {
            TLiteralExpression defaultOutputEntry = output.getDefaultOutputEntry();
            if (defaultOutputEntry != null) {
                return true;
            }
        }
        return false;
    }

    public TExpression expression(TNamedElement element) {
        if (element instanceof TDecision) {
            JAXBElement<? extends TExpression> expression = ((TDecision) element).getExpression();
            if (expression != null) {
                return expression.getValue();
            }
        } else if (element instanceof TBusinessKnowledgeModel) {
            TFunctionDefinition encapsulatedLogic = ((TBusinessKnowledgeModel) element).getEncapsulatedLogic();
            if (encapsulatedLogic != null) {
                JAXBElement<? extends TExpression> expression = encapsulatedLogic.getExpression();
                if (expression != null) {
                    return expression.getValue();
                }
            }
        } else if (element instanceof TInformationItem) {
            return null;
        } else {
            throw new UnsupportedOperationException(String.format("'%s' is not supported yet", element.getClass().getSimpleName()));
        }
        return null;
    }

    public boolean isLiteralExpression(TDRGElement element) {
        return expression(element) instanceof TLiteralExpression;
    }

    public boolean isFreeTextLiteralExpression(TNamedElement element) {
        TExpression expression = expression(element);
        return expression instanceof TLiteralExpression
                && DMNToJavaTransformer.FREE_TEXT_LANGUAGE.equals(((TLiteralExpression)expression).getExpressionLanguage());
    }

    public boolean isDecisionTableExpression(TDRGElement element) {
        return expression(element) instanceof TDecisionTable;
    }

    public boolean isCompoundDecisionTable(TDRGElement element) {
        TExpression expression = expression(element);
        return expression instanceof TDecisionTable
                && ((TDecisionTable) expression).getOutput() != null
                && ((TDecisionTable) expression).getOutput().size() > 1;
    }

    public boolean isInvocationExpression(TDRGElement element) {
        TExpression expression = expression(element);
        return expression instanceof TInvocation;
    }

    public boolean isContextExpression(TDRGElement element) {
        TExpression expression = expression(element);
        return expression instanceof TContext;
    }

    public boolean isRelationExpression(TDRGElement element) {
        TExpression expression = expression(element);
        return expression instanceof TRelation;
    }

    //
    // Item definition related functions
    //
    public boolean isEmpty(List<TItemDefinition> list) {
        return list == null || list.isEmpty();
    }

    public QName typeRef(TNamedElement element) {
        QName typeRef = null;
        if (element instanceof TInformationItem) {
            typeRef = ((TInformationItem) element).getTypeRef();
        }
        if (typeRef == null) {
            TInformationItem variable = variable(element);
            if (variable != null) {
                typeRef = variable.getTypeRef();
            }
        }
        if (typeRef == null) {
            TExpression expression = expression(element);
            if (expression != null) {
                typeRef = expression.getTypeRef();
            }
        }
        if (typeRef == null) {
            throw new DMNRuntimeException(String.format("Cannot resolve typeRef for element '%s'", element.getName()));
        }
        return typeRef;
    }

    //
    // Decision Service related functions
    //
    public TDecision getOutputDecision(TDecisionService decisionService) {
        List<TDMNElementReference> outputDecisionList = decisionService.getOutputDecision();
        if (outputDecisionList.size() != 1) {
            throw new DMNRuntimeException(String.format("Missing or more than one decision services in BKM '%s'", decisionService.getName()));
        }
        return this.findDecisionById(outputDecisionList.get(0).getHref());
    }

    //
    // DecisionTable related functions
    //
    public boolean isSingleHit(THitPolicy policy) {
        return policy == THitPolicy.FIRST
                || policy == THitPolicy.UNIQUE
                || policy == THitPolicy.ANY
                || policy == THitPolicy.PRIORITY
                || policy == null;
    }

    public boolean isFirstSingleHit(THitPolicy policy) {
        return policy == THitPolicy.FIRST;
    }

    public boolean atLeastTwoRules(TDecisionTable expression) {
        return expression.getRule() != null && expression.getRule().size() >= 2;
    }

    public boolean isMultipleHit(THitPolicy hitPolicy) {
        return THitPolicy.COLLECT == hitPolicy
                || THitPolicy.RULE_ORDER == hitPolicy
                || THitPolicy.OUTPUT_ORDER == hitPolicy
                ;
    }

    public boolean isOutputOrderHit(THitPolicy hitPolicy) {
        return THitPolicy.PRIORITY == hitPolicy
                || THitPolicy.OUTPUT_ORDER == hitPolicy
                ;
    }

    public boolean hasAggregator(TDecisionTable decisionTable) {
        return decisionTable.getAggregation() != null;
    }

    public int rulesCount(TDRGElement element) {
        TExpression expression = expression(element);
        if (expression instanceof TDecisionTable) {
            List<TDecisionRule> rules = ((TDecisionTable) expression).getRule();
            return rules == null ? 0 : rules.size();
        } else {
            return -1;
        }
    }

    public String outputClauseName(TDRGElement element, TOutputClause output) {
        // Try TOutputClause.typeRef
        String outputClauseName = output.getName();
        if (StringUtils.isBlank(outputClauseName)) {
            // Try variable.typeRef from parent
            if (!isCompoundDecisionTable(element)) {
                TInformationItem variable = variable(element);
                if (variable != null) {
                    outputClauseName = variable.getName();
                }
            }
            if (StringUtils.isBlank(outputClauseName)) {
                throw new DMNRuntimeException(String.format("Cannot resolve name for outputClause '%s' in element '%s'", output.getId(), element.getName()));
            }
        }
        return outputClauseName;
    }

    public TInformationItem variable(TNamedElement element) {
        if (element instanceof TInputData) {
            return ((TInputData) element).getVariable();
        } if (element instanceof TDecision) {
            return ((TDecision) element).getVariable();
        } else if (element instanceof TBusinessKnowledgeModel) {
            return ((TBusinessKnowledgeModel) element).getVariable();
        }
        return null;
    }

    public String name(TNamedElement element) {
        String name = element.getName();
        return name;
    }

    public String label(TDMNElement element) {
        String label = element.getLabel();
        return label == null ? "" : label.replace('\"', '\'');
    }

    public String displayName(TNamedElement element) {
        String name = label(element);
        if (StringUtils.isBlank(name)) {
            name = element.getName();
        }
        if (name == null) {
            throw new DMNRuntimeException(String.format("Display name cannot be null for element '%s'", element.getId()));
        }
        return name;
    }
}