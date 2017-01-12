{%- from 'gateway/settings.sls' import gateway with context %}

knox:
  pkg.installed

knox-master-secret:
  cmd.run:
    - name: /usr/hdp/current/knox-server/bin/knoxcli.sh create-master --master admin
    - user: knox
    - creates: /usr/hdp/current/knox-server/data/security/master

knox-create-cert:
  cmd.run:
    - name: /usr/hdp/current/knox-server/bin/knoxcli.sh create-cert --hostname {{ salt['grains.get']('fqdn') }}
    - user: knox
    - creates: /usr/hdp/current/knox-server/data/security/keystores/gateway.jks

/usr/hdp/current/knox-server/conf/topologies/hdc.xml:
  file.managed:
    - source: salt://gateway/config/hdc.xml
    - template: jinja
    - user: knox
    - group: knox


{% if gateway.is_systemd %}

/etc/systemd/system/knox-ldap.service:
  file.managed:
    - source: salt://gateway/systemd/knox-ldap.service

start-knox-ldap:
  module.wait:
    - name: service.systemctl_reload
    - watch:
      - file: /etc/systemd/system/knox-ldap.service
  service.running:
    - enable: True
    - name: knox-ldap
    - watch:
       - file: /etc/systemd/system/knox-ldap.service


/etc/systemd/system/knox-gateway.service:
  file.managed:
    - source: salt://gateway/systemd/knox-gateway.service

start-knox-gateway:
  module.wait:
    - name: service.systemctl_reload
    - watch:
      - file: /etc/systemd/system/knox-gateway.service
  service.running:
    - enable: True
    - name: knox-gateway
    - watch:
       - file: /etc/systemd/system/knox-gateway.service


{% endif %}


#salt -G 'roles:ambari_server' state.apply gateway

#curl -iku guest:guest-password -X GET 'https://localhost:8443/gateway/hdc/webhdfs/v1/?op=LISTSTATUS'

#rm -rfv /srv/salt/gateway; cp -rv /home/cloudbreak/gateway /srv/salt/; rm -rfv /home/cloudbreak/gateway
