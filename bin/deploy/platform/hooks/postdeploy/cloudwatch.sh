#!/bin/bash

cd /home/ec2-user

# get amazon cloudwatch agent and install it on the system
wget https://s3.amazonaws.com/amazoncloudwatch-agent/amazon_linux/amd64/latest/amazon-cloudwatch-agent.rpm
yum localinstall amazon-cloudwatch-agent.rpm

cat << 'EOF' > amazon-cloudwatch-agent.json
{
  "agent": {
    "metrics_collection_interval": 60
  },
  "metrics": {
    "append_dimensions": {
      "InstanceId": "${aws:InstanceId}"
    },
    "metrics_collected": {
      "mem": {
        "measurement": [
          "mem_total",
          "mem_free",
          "mem_used",
          "mem_used_percent",
          "mem_available",
          "mem_available_percent"
        ],
        "append_dimensions": {
          "AWSEBEnvironmentName": "__EB_ENV__"
        }
      },
      "cpu": {
        "measurement": [
          "cpu_time_active",
          "cpu_time_nice",
          "cpu_time_steal",
          "cpu_usage_active",
          "cpu_usage_idle",
          "cpu_usage_iowait"
        ],
        "append_dimensions": {
          "AWSEBEnvironmentName": "__EB_ENV__"
        }
      },
      "disk": {
        "measurement": [
          "disk_free",
          "disk_total",
          "disk_used",
          "disk_used_percent"
        ],
        "append_dimensions": {
          "AWSEBEnvironmentName": "__EB_ENV__"
        }
      }
    }
  }
}
EOF

# make sure the access right are correct
chmod 755 amazon-cloudwatch-agent.json
chown root:root amazon-cloudwatch-agent.json

# retrieve EB_ENV name and substitute it in the written file
EB_ENV="$(/opt/elasticbeanstalk/bin/get-config container -k environment_name)"
sed -i "s/__EB_ENV__/$EB_ENV/g" amazon-cloudwatch-agent.json

amazon-cloudwatch-agent-ctl -a append-config -m ec2 -c file:/home/ec2-user/amazon-cloudwatch-agent.json -s