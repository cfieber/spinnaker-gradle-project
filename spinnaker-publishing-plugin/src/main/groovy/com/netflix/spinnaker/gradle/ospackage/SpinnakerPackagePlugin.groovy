package com.netflix.spinnaker.gradle.ospackage

import com.netflix.gradle.plugins.application.OspackageApplicationPlugin
import com.netflix.gradle.plugins.packaging.ProjectPackagingExtension
import com.netflix.spinnaker.gradle.application.SpinnakerApplicationPlugin
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.redline_rpm.header.Os
import org.redline_rpm.payload.Directive

class SpinnakerPackagePlugin implements Plugin<Project> {

    @Override
    void apply(Project project) {
        if (project.plugins.hasPlugin('spinnaker.application')) {
            project.plugins.apply(OspackageApplicationPlugin)
            ProjectPackagingExtension extension = project.extensions.getByType(ProjectPackagingExtension)
            extension.setOs(Os.LINUX)
            String projVer = project.version.toString()
            int idx = projVer.indexOf('-')
            if (idx != -1 && !projVer.contains('-rc.')) {
                extension.setVersion(projVer.substring(0, idx))
            } else {
                extension.setVersion(projVer)
            }

            String appName = project.rootProject.name
            extension.setPackageName('spinnaker-' + appName)
            def postInstall = project.file('pkg_scripts/postInstall.sh')
            if (postInstall.exists()) {
                extension.postInstall(postInstall)
            }
            def postUninstall = project.file('pkg_scripts/postUninstall.sh')
            if (postUninstall.exists()) {
                extension.postUninstall(postUninstall)
            }

            def upstartConf = project.file("etc/init/${appName}.conf")
            if (upstartConf.exists()) {
                extension.from(upstartConf) {
                    into('/etc/init')
                    setUser('root')
                    setPermissionGroup('root')
                    setFileType(new Directive(Directive.RPMFILE_CONFIG | Directive.RPMFILE_NOREPLACE))
                }
            }

            def systemdService = project.file("lib/systemd/system/${appName}.service")
            if (systemdService.exists()) {
                extension.from(systemdService) {
                    into('/lib/systemd/system')
                    setUser('root')
                    setPermissionGroup('root')
                    setFileType(new Directive(Directive.RPMFILE_CONFIG | Directive.RPMFILE_NOREPLACE))
                }
            }

            def logrotateConf = project.file("etc/logrotate.d/${appName}")
            if (logrotateConf.exists()) {
                extension.from(logrotateConf) {
                    into('/etc/logrotate.d')
                    setUser('root')
                    setPermissionGroup('root')
                    setFileMode(0644)
                    setFileType(new Directive(Directive.RPMFILE_CONFIG | Directive.RPMFILE_NOREPLACE))
                }
            }
        }
    }
}
