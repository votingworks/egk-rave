#
# In order to securely call into the cryptography from Node/Typescript or Rust (the application code),
# given that the inputs and outputs of the cryptography are files,
# we use the following approach:
#
# 1/ create a file, mount it as a device via loopback
# 2/ use LUKS to encrypt that device
# 3/ mount the LUKS partition at /mnt/crypto-io
# 4/ copy all the input data to files at /mnt/crypto-io/input/*
# 5/ run the cryptography with appropriate input path /mnt/crypto-io/input/* and output /mnt/crypto-io/output/
# 6/ read the output in the output directory and store that in the app's database or send to the network
# 7/ unmount the loopback device
# 8/ delete the file
#
#
# This script does all of that
#
# inspiration from: https://gist.github.com/PaulMaddox/9972046
#


KEYFILE=/tmp/cryptoio-keyfile
FILE=/tmp/cryptoio.img
MOUNTPOINT=/mnt/cryptoio

mkdir -p $MOUNTPOINT

# create a place to store the keyfile
dd if=/dev/urandom of=$KEYFILE bs=1024 count=4
chmod 600 $KEYFILE

# create a file of the right size
# LUKS requires 16M for headers
dd if=/dev/zero of=$FILE bs=1M count=100

# TODO SET PERMISSIONS on .img file

sudo losetup -D
sudo losetup /dev/loop0 $FILE

# TODO: add keyfile so it's not asking interactively for passphrase
sudo cryptsetup -q -y luksFormat /dev/loop0 --key-file $KEYFILE

sudo cryptsetup luksOpen /dev/loop0 cryptoio --key-file $KEYFILE
sudo mkfs.ext4 /dev/mapper/cryptoio

sudo mount /dev/mapper/cryptoio $MOUNTPOINT

# delete the keyfile
shred $KEYFILE



