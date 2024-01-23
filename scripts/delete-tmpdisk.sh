#
# See create-tmpdisk.sh
#


FILE=/tmp/cryptoio.img
MOUNTPOINT=/mnt/cryptoio

sudo umount $MOUNTPOINT
sudo cryptsetup luksClose cryptoio
sudo losetup -D
sudo shred $FILE
sudo rm $FILE
