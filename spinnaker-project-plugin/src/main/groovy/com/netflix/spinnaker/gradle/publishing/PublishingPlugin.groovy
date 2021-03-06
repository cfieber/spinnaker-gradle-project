package com.netflix.spinnaker.gradle.publishing

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.JavaLibraryPlugin
import org.gradle.api.plugins.JavaPlatformPlugin
import org.gradle.api.publish.PublishingExtension
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.api.publish.maven.plugins.MavenPublishPlugin

class PublishingPlugin implements Plugin<Project> {

  public static final String PUBLICATION_NAME = "spinnaker"

  @Override
  void apply(Project project) {
    project.plugins.withType(JavaLibraryPlugin) {
      project.plugins.apply(MavenPublishPlugin)
      project.logger.info "adding maven publication for java library in $project.name"
      project.extensions.configure(PublishingExtension) { publishingExtension ->
        publishingExtension.publications.create(PUBLICATION_NAME, MavenPublication) { pub ->
          pub.from(project.components.getByName("java"))
          project.tasks.matching { it.name == 'sourceJar' }.configureEach {
            pub.artifact(it)
          }
        }
      }
    }

    project.plugins.withType(JavaPlatformPlugin) {
      project.plugins.apply(MavenPublishPlugin)
      project.logger.info "adding maven publication for java platform in $project.name"
      project.extensions.configure(PublishingExtension) { publishingExtension ->
        publishingExtension.publications.create(PUBLICATION_NAME, MavenPublication) { pub ->
          pub.from(project.components.getByName("javaPlatform"))
        }
      }
    }
  }
}
