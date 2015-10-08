# CloudFormation scripts

These scripts require the [AWS CloudFormation Command Line Tools][1].

 [1]: http://aws.amazon.com/developertools/2555753788650372

## Usage

  aws cloudformation create-stack --stack-name media-service-DEV-foo --template-body file://dev-template.json --region eu-west-1 --profile bar --capabilities CAPABILITY_IAM

## Troubleshooting

*An app server fails the ELB health check*

 * Check that the app server's security group allows ingress from the load
 balancer's security group
