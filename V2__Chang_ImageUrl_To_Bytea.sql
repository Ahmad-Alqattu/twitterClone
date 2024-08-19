-- Rename the columns and change the data type to BYTEA

-- For the tweets table
ALTER TABLE tweets
    RENAME COLUMN image_url TO image_data;

ALTER TABLE tweets
    ALTER COLUMN image_data TYPE BYTEA USING image_data::BYTEA;

-- For the users table
ALTER TABLE users
    RENAME COLUMN wallpaper_pic_url TO wallpaper_pic_data;
ALTER TABLE users
    RENAME COLUMN profile_pic_url TO profile_pic_data;

ALTER TABLE users
    ALTER COLUMN wallpaper_pic_data TYPE BYTEA USING wallpaper_pic_data::BYTEA,
    ALTER COLUMN profile_pic_data TYPE BYTEA USING profile_pic_data::BYTEA;
