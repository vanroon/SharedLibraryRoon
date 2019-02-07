package org.vanroon

class Person implements Serializable {
    private Map<String,Object> infos = new HashMap<>()

    protected def steps
    public def name

//    PipelineBuilder(steps){
//        this.steps = steps //
//    }
    Person(steps, name){
        this.steps = steps
        this.name = name
    }

    def addPersonInfos(String text){
        def yaml = steps.readYaml(text: text)
        if(yaml){
            infos.putAll(yaml)
        }
    }

    def sayName(){
        print "Hi, my name is: "
    }

}