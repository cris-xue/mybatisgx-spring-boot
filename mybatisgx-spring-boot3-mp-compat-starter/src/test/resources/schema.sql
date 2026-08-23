CREATE TABLE IF NOT EXISTS `t_user` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `name` VARCHAR(64),
    `code` VARCHAR(32),
    `status` INT,
    `age` INT,
    `salary` DECIMAL(12, 2),
    PRIMARY KEY (`id`)
);
