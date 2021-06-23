#!/bin/bash
set -eo pipefail

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

# Make sure dnsmadeeasy plugin is installed for cerbot
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

compartment_id={{ compartments.shared.compartment_ocid }}
vault_id={{ regions.ashburn.shared.vault.vault_ocid }}
key_id={{ regions.ashburn.shared.vault.master_key_ocid }}
echo ""
echo "Add certificate files to vault"
echo "compartment_id: $compartment_id"
echo "vault_id: $vault_id"
echo "key_id: $key_id"

for file in cert chain fullchain privkey
do
  secret_name="certificates_${cert_name}_${file}"
  secret_id=$(/usr/local/bin/oci vault secret list --auth instance_principal \
    --compartment-id {{ compartments.env_type.compartment_ocid }} \
    --name $secret_name | \
    jq -r '.data[0].id')
  secret_content="$(cat /etc/letsencrypt/live/${cert_name}/${file}.pem | base64)"
  if [ -z $secret_id ]
  then
    echo "Creating new secret '$secret_name'"
    /usr/local/bin/oci vault secret create-base64 \
      --auth instance_principal \
      --compartment-id $compartment_id \
      --vault-id $vault_id \
      --key-id $key_id \
      --secret-name "$secret_name" \
      --description "${cert_name} ${file}" \
      --secret-content-content "$secret_content" \
      --secret-content-stage CURRENT
  else
    echo "Secret '$secret_name' exists with secret_id: '$secret_id'. Getting current value"
    old_secret_content="$(/usr/local/bin/oci secrets secret-bundle get --auth instance_principal --secret-id $secret_id | \
      jq -r '.data."secret-bundle-content".content')"
    if [ "x$secret_content" != "x$old_secret_content" ]
    then
      echo "Updating secret '$secret_name' with id: '$secret_id'"
      /usr/local/bin/oci vault secret update-base64 \
        --auth instance_principal \
        --secret-id $secret_id \
        --secret-content-content "$secret_content"
    else
      echo "Existing version of secret matches new version of secret. Taking no action."
    fi
  fi
done
