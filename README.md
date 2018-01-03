# tibco-deployment
This is a rudimentary deployment framework for deploying TIBCO ActiveMatrix BusinessWorks™ 5.

The framework is based on [Apache Ant™](http://ant.apache.org/). Install ant, then run the build.xml script to make the ant-tibco-0.3.jar file.

An example TIBCO BusinessWorks 5 project to build and deploy. The EnvironmentSettings.xls (I know ;) file is used to keep track of global variables per environment. A macro in VBA (I know;) generates an XML that is used during deployment to make sure the right variable settings are used.

An example output of a sucessful deployment can be found in [sample_deployment.log](samples/EchoService/sample_deployment.log).
