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
        for (i in infos){
            def name = i.key
            steps.echo name
            for (j in name){
                steps.echo j.key
            }

        }
    }

    def sayName(){
        steps.echo "Hi, my name is: " + name
    }

}