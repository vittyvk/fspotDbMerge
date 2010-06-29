Merge two f-spot sqlite databases using sqlite4java

Usage: bin/fpotDbMerge.sh -l local_base -r remote_base -s source_db -d destination_db

example: ./bin/fpotDbMerge.sh -l /home/user1/Fotos -r /home/user2/Fotos -s /home/user1/.config/f-spot/photos.db -d /home/user2/.config/f-spot/photos.db
(assure appropriate read/write privileges)

This will merge all photos, rolls and versions from source to destination (only db changes, without copying photos,
rsync -a /home/user1/Fotos/ /home/user2/Fotos/ works well ). There is no tag syncing for now.
