//package com.lwx.lwxmagiccodebackend.config;
//
//@Configuration
//public class LangGraphStudioSampleConfig extends AbstractLangGraphStudioConfig {
//
//    final LangGraphFlow flow;
//
//    public LangGraphStudioSampleConfig() throws GraphStateException {
//        var workflow = new CodeGenWorkflow().createWorkflow().stateGraph;
//        // define your workflow
//        this.flow = LangGraphFlow.builder()
//                .title("LangGraph Studio")
//                .stateGraph(workflow)
//                .build();
//    }
//
//    @Override
//    public LangGraphFlow getFlow() {
//        return this.flow;
//    }
//}
