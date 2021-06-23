#!/bin/bash
set -e

######  Usage ############
usage() { echo "Usage: $0 -d profile_name
    -n is for the desired certificate name" 1>&2; exit 1; }
####### Variables and Inputs #################

while getopts ":d:o:n:" opt; do
  case "${opt}" in
    d)
      domain_name=${OPTARG}
      ;;
    n)
      cert_name=${OPTARG}
      ;;
  esac
done
shift $((OPTIND-1))

if [ -z "${domain_name}" ] || [ -z "${cert_name}" ]
then
  usage
fi

# Run as root
DATE=`date +%m.%d.%Y`
CERTBOT="/usr/local/bin/certbot-auto"

# Make sure dnsmadeeasy plugin is installed for cerbots
/opt/eff.org/certbot/venv/bin/pip install certbot-dns-dnsmadeeasy

echo ""
echo "============================"
echo "Letsencrypt for $domain_name"

echo "certbot renew w/DNS DCV -- 2mins: Makes temporary txt record (ex. _acme-challenge.staging.my.domain.)"
timeout 300 $CERTBOT certonly --non-interactive --no-self-upgrade --agree-tos --email it@my.domain \
  -d "${domain_name}" --cert-name "${cert_name}" --server https://acme-v02.api.letsencrypt.org/directory \
  --dns-dnsmadeeasy --dns-dnsmadeeasy-credentials /root/scripts/letsencrypt/certbot_dnsmadeeasy_creds \
  --dns-dnsmadeeasy-propagation-seconds 120  || exit 1
echo "certbot done"

ls -al /etc/letsencrypt/live/${cert_name}

echo ""
echo "Add certificate files to vault"
export VAULT_ADDR=http://vault01.shared-resources.my.domain:8200
/usr/local/bin/vault login -method=oci auth_type=instance role=provisioner-role
/usr/local/bin/vault kv put kv/certificates/${cert_name} cert=@/etc/letsencrypt/live/${cert_name}/cert.pem \
                                          chain=@/etc/letsencrypt/live/${cert_name}/chain.pem \
                                          fullchain=@/etc/letsencrypt/live/${cert_name}/fullchain.pem \
                                          privkey=@/etc/letsencrypt/live/${cert_name}/privkey.pem
