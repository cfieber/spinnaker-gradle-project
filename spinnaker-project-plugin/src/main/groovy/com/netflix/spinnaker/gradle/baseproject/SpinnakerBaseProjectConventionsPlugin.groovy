package com.netflix.spinnaker.gradle.baseproject

import com.netflix.spinnaker.gradle.Flags
import groovy.transform.CompileStatic
import org.gradle.api.JavaVersion
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.java.archives.Manifest
import org.gradle.api.plugins.BasePlugin
import org.gradle.api.plugins.JavaBasePlugin
import org.gradle.api.plugins.JavaLibraryPlugin
import org.gradle.api.plugins.JavaPluginConvention
import org.gradle.api.publish.maven.plugins.MavenPublishPlugin
import org.gradle.api.tasks.Delete
import org.gradle.api.tasks.javadoc.Javadoc
import org.gradle.jvm.tasks.Jar

@CompileStatic
class SpinnakerBaseProjectConventionsPlugin implements Plugin<Project> {
    @Override
    void apply(Project project) {
      if (project.version == Project.DEFAULT_VERSION) {
        project.version = Flags.DEFAULT_VERSION_WHEN_UNSPECIFIED
      }

      project.plugins.withType(JavaBasePlugin) {
        project.plugins.apply(MavenPublishPlugin)
        project.repositories.jcenter()
        JavaPluginConvention convention = project.convention.getPlugin(JavaPluginConvention)
        convention.sourceCompatibility = JavaVersion.VERSION_1_8
        convention.targetCompatibility = JavaVersion.VERSION_1_8
      }
      project.plugins.withType(JavaLibraryPlugin) {
        JavaPluginConvention convention = project.convention.getPlugin(JavaPluginConvention)
        def sourceJar = project.tasks.create("sourceJar", Jar)
        sourceJar.dependsOn("classes")
        sourceJar.archiveClassifier.set('sources')
        sourceJar.from(convention.sourceSets.getByName('main').allSource)
        project.artifacts.add('archives', sourceJar)
      }
      // Nebula insists on building Javadoc, but we don't do anything with it
      // and it seems to cause lots of errors.
      project.tasks.withType(Javadoc) { (it as Javadoc).setFailOnError(false) }
      project.tasks.withType(Jar) { setImplementationOssVersion((it as Jar), project) }

      project.plugins.withType(BasePlugin) {
        Delete clean = project.getTasks().getByName(BasePlugin.CLEAN_TASK_NAME) as Delete
        clean.delete("${project.projectDir}/plugins")
      }
    }

  /**
   * If the property "ossVersion" exists the MANIFEST.MF "Implementation-Version" attribute
   * will be set to the corresponding property. This can be used to support use cases where services
   * are being extended and rebuilt.  Unless you're re-building services, this is likely unnecessary
   * and the default value of the attribute "Implementation-Version" will suffice for determining
   * the service version.
   */
    private static void setImplementationOssVersion(Jar jar, Project project) {
      String ossVersionProperty = "ossVersion"
      if (project.hasProperty(ossVersionProperty)) {
        jar.manifest {
          (it as Manifest).attributes(["Implementation-Version": project.property(ossVersionProperty)])
        }
      }
    }
}
