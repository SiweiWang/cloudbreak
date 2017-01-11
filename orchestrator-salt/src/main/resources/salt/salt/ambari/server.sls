{%- from 'ambari/settings.sls' import ambari with context %}

{% if not ambari.is_predefined_repo %}

include:
  - ambari.repo

ambari-server:
  pkg.installed:
    - require:
      - sls: ambari.repo
    - version: {{ ambari.version }}

{% else %}

lazy_view_load:
  file.append:
    - name: /etc/ambari-server/conf/ambari.properties
    - text: view.extract-after-cluster-config=true
    - unless: cat /etc/ambari-server/conf/ambari.properties | grep view.extract-after-cluster-config

install_mysql:
  pkg.installed:
    - pkgs:
      - mysql55-server
      - mysql

packages_pre_installed:
  file.append:
    - name: /etc/ambari-server/conf/ambari.properties
    - text: packages.pre.installed=true
    - unless: cat /etc/ambari-server/conf/ambari.properties | grep packages.pre.installed

parallel_topology_task_execution:
  file.append:
    - name: /etc/ambari-server/conf/ambari.properties
    - text: topology.task.creation.parallel=true
    - unless: cat /etc/ambari-server/conf/ambari.properties | grep topology.task.creation.parallel

disable_agent_cache_update:
  file.append:
    - name: /etc/ambari-server/conf/ambari.properties
    - text: agent.auto.cache.update=false
    - unless: cat /etc/ambari-server/conf/ambari.properties | grep agent.auto.cache.update

{% endif %}

{% if ambari.ambari_database.vendor == 'mysql' %}
install-mariadb:
  pkg.installed:
    - pkgs:
      - mariadb
{% endif %}

/opt/hadoop-classpath.sh:
  file.managed:
    - makedirs: True
    - source: salt://ambari/scripts/hadoop-classpath.sh
    - mode: 744

extend_hadoop_classpath:
  cmd.run:
    - name: /opt/hadoop-classpath.sh

/var/lib/ambari-server/jdbc-drivers:
  cmd.run:
    - name: cp -R /opt/jdbc-drivers /var/lib/ambari-server/jdbc-drivers
    - unless: ls -1 /var/lib/ambari-server/jdbc-drivers

/opt/ambari-server/ambari-server-init.sh:
  file.managed:
    - makedirs: True
    - source: salt://ambari/scripts/ambari-server-init.sh
    - template: jinja
    - context:
      ambari_database: {{ ambari.ambari_database }}
    - mode: 744

set_install_timeout:
  file.replace:
    - name: /etc/ambari-server/conf/ambari.properties
    - pattern: "agent.package.install.task.timeout=1800"
    - repl: "agent.package.install.task.timeout=3600"

{% if ambari.is_systemd %}

/etc/systemd/system/ambari-server.service:
  file.managed:
    - source: salt://ambari/systemd/ambari-server.service

start-ambari-server:
  module.wait:
    - name: service.systemctl_reload
    - watch:
      - file: /etc/systemd/system/ambari-server.service
  service.running:
    - enable: True
    - name: ambari-server
    - watch:
       - file: /etc/systemd/system/ambari-server.service

{% else %}

# Upstart case

# Avoid concurrency between SysV and Upstart
disable-ambari-server-sysv:
  cmd.run:
    - name: chkconfig ambari-server off
    - onlyif: chkconfig --list ambari-server | grep on

/etc/init/ambari-server.override:
  file.managed:
    - source: salt://ambari/upstart/ambari-server.override

start-ambari-server:
  service.running:
    - enable: True
    - name: ambari-server

{% endif %}
