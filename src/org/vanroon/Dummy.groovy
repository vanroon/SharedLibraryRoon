package org.vanroon

class Dummy extends PipelineBuilder {
    private Map<String,Object> infos = new HashMap<>()

//    protected def steps
    public def name

//    PipelineBuilder(steps){
//        this.steps = steps //
//    }
    Dummy(steps, name){
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
            for (j in infos[name]){
                def name2 = j.key
                steps.echo name2
            }

        }
        steps.echo "now printing the  vars"
        steps.echo "infos"
        for ( i in infos ){
            def person = infos[i]
//            def env = envs[envName]
//            def environment = env['parameters']['environment']
//            def city = person['House_params']['City']
            steps.echo city
        }

    }

    def sayName(){
        steps.echo "Hi, my name is: " + name
    }

    userId = findUser()
    echo userId

}