#!/usr/bin/env groovy
package org.vanroon

// all classes in **/src/* need to be imported specifically and invoked:
// @Library('LIBNAME')
// import package.classname
// ...
// script {
// z = new package.classname()
// }
class GlobalVars {
	static String foo = "bar";
}
