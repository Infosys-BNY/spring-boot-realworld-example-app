create table users (
  id varchar(255) primary key,
  username varchar(255) UNIQUE,
  password varchar(255),
  email varchar(255) UNIQUE,
  bio text,
  image varchar(511)
);

create table articles (
  id varchar(255) primary key,
  user_id varchar(255),
  slug varchar(255) UNIQUE,
  title varchar(255),
  description text,
  body text,
  created_at TIMESTAMP NOT NULL,
  updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

create table article_favorites (
  article_id varchar(255) not null,
  user_id varchar(255) not null,
  primary key(article_id, user_id)
);

create table follows (
  user_id varchar(255) not null,
  follow_id varchar(255) not null
);

create table tags (
  id varchar(255) primary key,
  name varchar(255) not null
);

create table article_tags (
  article_id varchar(255) not null,
  tag_id varchar(255) not null
);

create table comments (
  id varchar(255) primary key,
  body text,
  article_id varchar(255),
  user_id varchar(255),
  created_at TIMESTAMP NOT NULL,
  updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

create table comment_reactions (
  id varchar(255) primary key,
  comment_id varchar(255) not null,
  user_id varchar(255) not null,
  reaction_type varchar(10) not null,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  foreign key (comment_id) references comments(id) on delete cascade,
  foreign key (user_id) references users(id) on delete cascade,
  unique(comment_id, user_id)
);

create index idx_comment_reactions_comment_id on comment_reactions(comment_id);
create index idx_comment_reactions_user_id on comment_reactions(user_id);
