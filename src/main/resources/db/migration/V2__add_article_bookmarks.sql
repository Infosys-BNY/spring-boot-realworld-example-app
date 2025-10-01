create table article_bookmarks (
  article_id varchar(255) not null,
  user_id varchar(255) not null,
  primary key(article_id, user_id),
  foreign key (article_id) references articles(id) on delete cascade,
  foreign key (user_id) references users(id) on delete cascade
);
