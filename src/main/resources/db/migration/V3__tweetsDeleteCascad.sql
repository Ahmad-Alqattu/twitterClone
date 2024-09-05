ALTER TABLE comments
DROP CONSTRAINT comments_tweet_id_fkey,
ADD CONSTRAINT comments_tweet_id_fkey
FOREIGN KEY (tweet_id)
REFERENCES tweets(id)
ON DELETE CASCADE;

ALTER TABLE likes
DROP CONSTRAINT likes_tweet_id_fkey,
ADD CONSTRAINT likes_tweet_id_fkey
FOREIGN KEY (tweet_id)
REFERENCES tweets(id)
ON DELETE CASCADE;

ALTER TABLE retweets
DROP CONSTRAINT retweets_tweet_id_fkey,
ADD CONSTRAINT retweets_tweet_id_fkey
FOREIGN KEY (tweet_id)
REFERENCES tweets(id)
ON DELETE CASCADE;