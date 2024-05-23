
insert into `users` (`id`, `email`, `nickname`, `address`, `certification_code`, `status`, `last_login_at`)
values (1, 'go1323@test.com', 'go1323', 'Seoul', 'aaaaaaaaaa', 'ACTIVE', 0);

insert into `users` (`id`, `email`, `nickname`, `address`, `certification_code`, `status`, `last_login_at`)
values (2, 'go13231@test.com', 'go13231', 'Seoul', 'bbbbbbbbbb', 'PENDING', 0);

ALTER TABLE `users` alter column `id` restart with 3;