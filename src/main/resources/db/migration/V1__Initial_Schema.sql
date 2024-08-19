--
-- PostgreSQL database dump
--

-- Dumped from database version 16.4 (Debian 16.4-1.pgdg120+1)
-- Dumped by pg_dump version 16.4 (Debian 16.4-1.pgdg120+1)

SET statement_timeout = 0;
SET lock_timeout = 0;
SET idle_in_transaction_session_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;
SELECT pg_catalog.set_config('search_path', '', false);
SET check_function_bodies = false;
SET xmloption = content;
SET client_min_messages = warning;
SET row_security = off;

--
-- Name: public; Type: SCHEMA; Schema: -; Owner: postgres
--

-- *not* creating schema, since initdb creates it


ALTER SCHEMA public OWNER TO postgres;

SET default_tablespace = '';

SET default_table_access_method = heap;

--
-- Name: comments; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.comments (
                                 id integer NOT NULL,
                                 user_id integer,
                                 tweet_id integer,
                                 content text NOT NULL,
                                 created_at timestamp with time zone DEFAULT CURRENT_TIMESTAMP
);


ALTER TABLE public.comments OWNER TO postgres;

--
-- Name: comments_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.comments_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.comments_id_seq OWNER TO postgres;

--
-- Name: comments_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.comments_id_seq OWNED BY public.comments.id;


-- Name: followers; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.followers (
                                  id integer NOT NULL,
                                  follower_id integer,
                                  followed_id integer,
                                  created_at timestamp with time zone DEFAULT CURRENT_TIMESTAMP
);


ALTER TABLE public.followers OWNER TO postgres;

--
-- Name: followers_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.followers_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.followers_id_seq OWNER TO postgres;

--
-- Name: followers_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.followers_id_seq OWNED BY public.followers.id;


--
-- Name: likes; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.likes (
                              id integer NOT NULL,
                              user_id integer,
                              tweet_id integer,
                              created_at timestamp with time zone DEFAULT CURRENT_TIMESTAMP
);


ALTER TABLE public.likes OWNER TO postgres;

--
-- Name: likes_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.likes_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.likes_id_seq OWNER TO postgres;

--
-- Name: likes_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.likes_id_seq OWNED BY public.likes.id;


--
-- Name: retweets; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.retweets (
                                 id integer NOT NULL,
                                 user_id integer,
                                 tweet_id integer,
                                 content text,
                                 created_at timestamp with time zone DEFAULT CURRENT_TIMESTAMP
);


ALTER TABLE public.retweets OWNER TO postgres;

--
-- Name: retweets_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.retweets_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.retweets_id_seq OWNER TO postgres;

--
-- Name: retweets_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.retweets_id_seq OWNED BY public.retweets.id;


--
-- Name: tweets; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.tweets (
                               id integer NOT NULL,
                               user_id integer,
                               content text NOT NULL,
                               image_data bytea,
                               created_at timestamp with time zone DEFAULT CURRENT_TIMESTAMP
);


ALTER TABLE public.tweets OWNER TO postgres;

--
-- Name: tweets_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.tweets_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.tweets_id_seq OWNER TO postgres;

--
-- Name: tweets_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.tweets_id_seq OWNED BY public.tweets.id;


--
-- Name: users; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.users (
                              id integer NOT NULL,
                              username character varying(50) NOT NULL,
                              email character varying(100) NOT NULL,
                              password_hash character varying(255) NOT NULL,
                              profile_pic_data bytea,
                              wallpaper_pic_data bytea,
                              created_at timestamp with time zone DEFAULT CURRENT_TIMESTAMP,
                              bio text
);


ALTER TABLE public.users OWNER TO postgres;

--
-- Name: users_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.users_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.users_id_seq OWNER TO postgres;

--
-- Name: users_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.users_id_seq OWNED BY public.users.id;


--
-- Name: comments id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.comments ALTER COLUMN id SET DEFAULT nextval('public.comments_id_seq'::regclass);


--
-- Name: followers id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.followers ALTER COLUMN id SET DEFAULT nextval('public.followers_id_seq'::regclass);


--
-- Name: likes id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.likes ALTER COLUMN id SET DEFAULT nextval('public.likes_id_seq'::regclass);


--
-- Name: retweets id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.retweets ALTER COLUMN id SET DEFAULT nextval('public.retweets_id_seq'::regclass);


--
-- Name: tweets id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.tweets ALTER COLUMN id SET DEFAULT nextval('public.tweets_id_seq'::regclass);


--
-- Name: users id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.users ALTER COLUMN id SET DEFAULT nextval('public.users_id_seq'::regclass);


--
-- Name: comments comments_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.comments
    ADD CONSTRAINT comments_pkey PRIMARY KEY (id);

--
-- Name: followers followers_follower_id_followed_id_key; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.followers
    ADD CONSTRAINT followers_follower_id_followed_id_key UNIQUE (follower_id, followed_id);


--
-- Name: followers followers_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.followers
    ADD CONSTRAINT followers_pkey PRIMARY KEY (id);


--
-- Name: likes likes_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.likes
    ADD CONSTRAINT likes_pkey PRIMARY KEY (id);


--
-- Name: likes likes_user_id_tweet_id_key; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.likes
    ADD CONSTRAINT likes_user_id_tweet_id_key UNIQUE (user_id, tweet_id);


--
-- Name: retweets retweets_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.retweets
    ADD CONSTRAINT retweets_pkey PRIMARY KEY (id);


--
-- Name: tweets tweets_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.tweets
    ADD CONSTRAINT tweets_pkey PRIMARY KEY (id);


--
-- Name: users users_email_key; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.users
    ADD CONSTRAINT users_email_key UNIQUE (email);


--
-- Name: users users_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.users
    ADD CONSTRAINT users_pkey PRIMARY KEY (id);


--
-- Name: users users_username_key; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.users
    ADD CONSTRAINT users_username_key UNIQUE (username);





--
-- Name: idx_comments_tweet_id; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_comments_tweet_id ON public.comments USING btree (tweet_id);


--
-- Name: idx_comments_user_id; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_comments_user_id ON public.comments USING btree (user_id);


--
-- Name: idx_likes_tweet_id; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_likes_tweet_id ON public.likes USING btree (tweet_id);


--
-- Name: idx_likes_user_id; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_likes_user_id ON public.likes USING btree (user_id);


--
-- Name: comments comments_tweet_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.comments
    ADD CONSTRAINT comments_tweet_id_fkey FOREIGN KEY (tweet_id) REFERENCES public.tweets(id);


--
-- Name: comments comments_user_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.comments
    ADD CONSTRAINT comments_user_id_fkey FOREIGN KEY (user_id) REFERENCES public.users(id);


--
-- Name: followers followers_followed_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.followers
    ADD CONSTRAINT followers_followed_id_fkey FOREIGN KEY (followed_id) REFERENCES public.users(id);


--
-- Name: followers followers_follower_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.followers
    ADD CONSTRAINT followers_follower_id_fkey FOREIGN KEY (follower_id) REFERENCES public.users(id);


--
-- Name: likes likes_tweet_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.likes
    ADD CONSTRAINT likes_tweet_id_fkey FOREIGN KEY (tweet_id) REFERENCES public.tweets(id);


--
-- Name: likes likes_user_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.likes
    ADD CONSTRAINT likes_user_id_fkey FOREIGN KEY (user_id) REFERENCES public.users(id);


--
-- Name: retweets retweets_tweet_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.retweets
    ADD CONSTRAINT retweets_tweet_id_fkey FOREIGN KEY (tweet_id) REFERENCES public.tweets(id);


--
-- Name: retweets retweets_user_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.retweets
    ADD CONSTRAINT retweets_user_id_fkey FOREIGN KEY (user_id) REFERENCES public.users(id);


--
-- Name: tweets tweets_user_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.tweets
    ADD CONSTRAINT tweets_user_id_fkey FOREIGN KEY (user_id) REFERENCES public.users(id);


--
-- Name: SCHEMA public; Type: ACL; Schema: -; Owner: postgres
--

REVOKE USAGE ON SCHEMA public FROM PUBLIC;


--
-- PostgreSQL database dump complete
--

