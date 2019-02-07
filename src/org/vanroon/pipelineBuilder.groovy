package org.vanroon

class PipelineBuilder implements Serializable {
    protected def steps

    PipelineBuilder(steps){
        this.steps = steps
    }
}