CREATE TABLE `hwsj`.`cmf_activity`  (
  `id` varchar(36) NOT NULL,
  `uid` varchar(12) NULL DEFAULT '' COMMENT '用户id',
  `type` varchar(255) NULL DEFAULT '' COMMENT '参与活动',
  `nums` int(8) NULL COMMENT '活动参与次数',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
);

CREATE TABLE `cmf_anchor_resource` (
  `id` int(8) NOT NULL AUTO_INCREMENT,
  `uid` varchar(12) NOT NULL DEFAULT '' COMMENT '机器人主播id',
  `country_code` varchar(12) NOT NULL DEFAULT '' COMMENT '国家',
  `url` varchar(255) NOT NULL DEFAULT '' COMMENT '资源url',
  `cover` varchar(255) NOT NULL DEFAULT '' COMMENT '视频封面',
  `type` varchar(8) NOT NULL DEFAULT '' COMMENT '资源类型:photo/video',
  `tag` int(1) NOT NULL DEFAULT '0' COMMENT '尺度：0标准/1涉黄',
  `use_type` int(2) NOT NULL DEFAULT '0' COMMENT '视频类型,0通话/',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
);

INSERT INTO `hwsj`.`cmf_anchor_resource`(`uid`, `url`, `cover`, `type`, `tag`, `use_type`) VALUES ('354642', 'http://res.oyechat.club/videos/anchor_resource/nomal/354642_0_0_1.mp4', 'http://res.oyechat.club/videos/robot_resource/nomal/cover/354642_0_0_1.jpg', 'video', 0, 0);
INSERT INTO `hwsj`.`cmf_anchor_resource`(`uid`, `url`, `cover`, `type`, `tag`, `use_type`) VALUES ('700006', 'http://res.oyechat.club/videos/anchor_resource/nomal/700006_0_0_1.mp4', 'http://res.oyechat.club/videos/robot_resource/nomal/cover/700006_0_0_1.jpg', 'video', 0, 0);
INSERT INTO `hwsj`.`cmf_anchor_resource`(`uid`, `url`, `cover`, `type`, `tag`, `use_type`) VALUES ('338566', 'http://res.oyechat.club/videos/anchor_resource/nomal/338566_0_0_1.mp4', 'http://res.oyechat.club/videos/robot_resource/nomal/cover/338566_0_0_1.jpg', 'video', 0, 0);
INSERT INTO `hwsj`.`cmf_anchor_resource`(`uid`, `url`, `cover`, `type`, `tag`, `use_type`) VALUES ('410243', 'http://res.oyechat.club/videos/anchor_resource/nomal/410243_0_0_1.mp4', 'http://res.oyechat.club/videos/robot_resource/nomal/cover/410243_0_0_1.jpg', 'video', 0, 0);
INSERT INTO `hwsj`.`cmf_anchor_resource`(`uid`, `url`, `cover`, `type`, `tag`, `use_type`) VALUES ('462191', 'http://res.oyechat.club/videos/anchor_resource/nomal/462191_0_0_1.mp4', 'http://res.oyechat.club/videos/robot_resource/nomal/cover/462191_0_0_1.jpg', 'video', 0, 0);
INSERT INTO `hwsj`.`cmf_anchor_resource`(`uid`, `url`, `cover`, `type`, `tag`, `use_type`) VALUES ('197081', 'http://res.oyechat.club/videos/anchor_resource/nomal/197081_0_0_1.mp4', 'http://res.oyechat.club/videos/robot_resource/nomal/cover/197081_0_0_1.jpg', 'video', 0, 0);
INSERT INTO `hwsj`.`cmf_anchor_resource`(`uid`, `url`, `cover`, `type`, `tag`, `use_type`) VALUES ('692127', 'http://res.oyechat.club/videos/anchor_resource/nomal/692127_0_0_1.mp4', 'http://res.oyechat.club/videos/robot_resource/nomal/cover/692127_0_0_1.jpg', 'video', 0, 0);
INSERT INTO `hwsj`.`cmf_anchor_resource`(`uid`, `url`, `cover`, `type`, `tag`, `use_type`) VALUES ('336934', 'http://res.oyechat.club/videos/anchor_resource/nomal/336934_0_0_1.mp4', 'http://res.oyechat.club/videos/robot_resource/nomal/cover/336934_0_0_1.jpg', 'video', 0, 0);
INSERT INTO `hwsj`.`cmf_anchor_resource`(`uid`, `url`, `cover`, `type`, `tag`, `use_type`) VALUES ('696830', 'http://res.oyechat.club/videos/anchor_resource/nomal/696830_0_0_1.mp4', 'http://res.oyechat.club/videos/robot_resource/nomal/cover/696830_0_0_1.jpg', 'video', 0, 0);
INSERT INTO `hwsj`.`cmf_anchor_resource`(`uid`, `url`, `cover`, `type`, `tag`, `use_type`) VALUES ('310661', 'http://res.oyechat.club/videos/anchor_resource/nomal/310661_0_0_1.mp4', 'http://res.oyechat.club/videos/robot_resource/nomal/cover/310661_0_0_1.jpg', 'video', 0, 0);
INSERT INTO `hwsj`.`cmf_anchor_resource`(`uid`, `url`, `cover`, `type`, `tag`, `use_type`) VALUES ('701686', 'http://res.oyechat.club/videos/anchor_resource/nomal/701686_0_0_1.mp4', 'http://res.oyechat.club/videos/robot_resource/nomal/cover/701686_0_0_1.jpg', 'video', 0, 0);
INSERT INTO `hwsj`.`cmf_anchor_resource`(`uid`, `url`, `cover`, `type`, `tag`, `use_type`) VALUES ('674444', 'http://res.oyechat.club/videos/anchor_resource/nomal/674444_0_0_1.mp4', 'http://res.oyechat.club/videos/robot_resource/nomal/cover/674444_0_0_1.jpg', 'video', 0, 0);
INSERT INTO `hwsj`.`cmf_anchor_resource`(`uid`, `url`, `cover`, `type`, `tag`, `use_type`) VALUES ('700335', 'http://res.oyechat.club/videos/anchor_resource/nomal/700335_0_0_1.mp4', 'http://res.oyechat.club/videos/robot_resource/nomal/cover/700335_0_0_1.jpg', 'video', 0, 0);
INSERT INTO `hwsj`.`cmf_anchor_resource`(`uid`, `url`, `cover`, `type`, `tag`, `use_type`) VALUES ('700300', 'http://res.oyechat.club/videos/anchor_resource/nomal/700300_0_0_1.mp4', 'http://res.oyechat.club/videos/robot_resource/nomal/cover/700300_0_0_1.jpg', 'video', 0, 0);
INSERT INTO `hwsj`.`cmf_anchor_resource`(`uid`, `url`, `cover`, `type`, `tag`, `use_type`) VALUES ('696809', 'http://res.oyechat.club/videos/anchor_resource/nomal/696809_0_0_1.mp4', 'http://res.oyechat.club/videos/robot_resource/nomal/cover/696809_0_0_1.jpg', 'video', 0, 0);
INSERT INTO `hwsj`.`cmf_anchor_resource`(`uid`, `url`, `cover`, `type`, `tag`, `use_type`) VALUES ('650149', 'http://res.oyechat.club/videos/anchor_resource/nomal/650149_0_0_1.mp4', 'http://res.oyechat.club/videos/robot_resource/nomal/cover/650149_0_0_1.jpg', 'video', 0, 0);
INSERT INTO `hwsj`.`cmf_anchor_resource`(`uid`, `url`, `cover`, `type`, `tag`, `use_type`) VALUES ('297356', 'http://res.oyechat.club/videos/anchor_resource/nomal/297356_0_0_1.mp4', 'http://res.oyechat.club/videos/robot_resource/nomal/cover/297356_0_0_1.jpg', 'video', 0, 0);
INSERT INTO `hwsj`.`cmf_anchor_resource`(`uid`, `url`, `cover`, `type`, `tag`, `use_type`) VALUES ('688087', 'http://res.oyechat.club/videos/anchor_resource/nomal/688087_0_0_1.mp4', 'http://res.oyechat.club/videos/robot_resource/nomal/cover/688087_0_0_1.jpg', 'video', 0, 0);
INSERT INTO `hwsj`.`cmf_anchor_resource`(`uid`, `url`, `cover`, `type`, `tag`, `use_type`) VALUES ('696639', 'http://res.oyechat.club/videos/anchor_resource/nomal/696639_0_0_1.mp4', 'http://res.oyechat.club/videos/robot_resource/nomal/cover/696639_0_0_1.jpg', 'video', 0, 0);
INSERT INTO `hwsj`.`cmf_anchor_resource`(`uid`, `url`, `cover`, `type`, `tag`, `use_type`) VALUES ('697837', 'http://res.oyechat.club/videos/anchor_resource/nomal/697837_0_0_1.mp4', 'http://res.oyechat.club/videos/robot_resource/nomal/cover/697837_0_0_1.jpg', 'video', 0, 0);
INSERT INTO `hwsj`.`cmf_anchor_resource`(`uid`, `url`, `cover`, `type`, `tag`, `use_type`) VALUES ('676386', 'http://res.oyechat.club/videos/anchor_resource/nomal/676386_0_0_1.mp4', 'http://res.oyechat.club/videos/robot_resource/nomal/cover/676386_0_0_1.jpg', 'video', 0, 0);
INSERT INTO `hwsj`.`cmf_anchor_resource`(`uid`, `url`, `cover`, `type`, `tag`, `use_type`) VALUES ('681307', 'http://res.oyechat.club/videos/anchor_resource/nomal/681307_0_0_1.mp4', 'http://res.oyechat.club/videos/robot_resource/nomal/cover/681307_0_0_1.jpg', 'video', 0, 0);
INSERT INTO `hwsj`.`cmf_anchor_resource`(`uid`, `url`, `cover`, `type`, `tag`, `use_type`) VALUES ('666026', 'http://res.oyechat.club/videos/anchor_resource/nomal/666026_0_0_1.mp4', 'http://res.oyechat.club/videos/robot_resource/nomal/cover/666026_0_0_1.jpg', 'video', 0, 0);
INSERT INTO `hwsj`.`cmf_anchor_resource`(`uid`, `url`, `cover`, `type`, `tag`, `use_type`) VALUES ('242788', 'http://res.oyechat.club/videos/anchor_resource/nomal/242788_0_0_1.mp4', 'http://res.oyechat.club/videos/robot_resource/nomal/cover/242788_0_0_1.jpg', 'video', 0, 0);
INSERT INTO `hwsj`.`cmf_anchor_resource`(`uid`, `url`, `cover`, `type`, `tag`, `use_type`) VALUES ('696815', 'http://res.oyechat.club/videos/anchor_resource/nomal/696815_0_0_1.mp4', 'http://res.oyechat.club/videos/robot_resource/nomal/cover/696815_0_0_1.jpg', 'video', 0, 0);
INSERT INTO `hwsj`.`cmf_anchor_resource`(`uid`, `url`, `cover`, `type`, `tag`, `use_type`) VALUES ('326326', 'http://res.oyechat.club/videos/anchor_resource/nomal/326326_0_0_1.mp4', 'http://res.oyechat.club/videos/robot_resource/nomal/cover/326326_0_0_1.jpg', 'video', 0, 0);
INSERT INTO `hwsj`.`cmf_anchor_resource`(`uid`, `url`, `cover`, `type`, `tag`, `use_type`) VALUES ('314198', 'http://res.oyechat.club/videos/anchor_resource/nomal/314198_0_0_1.mp4', 'http://res.oyechat.club/videos/robot_resource/nomal/cover/314198_0_0_1.jpg', 'video', 0, 0);
INSERT INTO `hwsj`.`cmf_anchor_resource`(`uid`, `url`, `cover`, `type`, `tag`, `use_type`) VALUES ('642576', 'http://res.oyechat.club/videos/anchor_resource/nomal/642576_0_0_1.mp4', 'http://res.oyechat.club/videos/robot_resource/nomal/cover/642576_0_0_1.jpg', 'video', 0, 0);
INSERT INTO `hwsj`.`cmf_anchor_resource`(`uid`, `url`, `cover`, `type`, `tag`, `use_type`) VALUES ('672808', 'http://res.oyechat.club/videos/anchor_resource/nomal/672808_0_0_1.mp4', 'http://res.oyechat.club/videos/robot_resource/nomal/cover/672808_0_0_1.jpg', 'video', 0, 0);
INSERT INTO `hwsj`.`cmf_anchor_resource`(`uid`, `url`, `cover`, `type`, `tag`, `use_type`) VALUES ('701959', 'http://res.oyechat.club/videos/anchor_resource/nomal/701959_0_0_1.mp4', 'http://res.oyechat.club/videos/robot_resource/nomal/cover/701959_0_0_1.jpg', 'video', 0, 0);
INSERT INTO `hwsj`.`cmf_anchor_resource`(`uid`, `url`, `cover`, `type`, `tag`, `use_type`) VALUES ('610474', 'http://res.oyechat.club/videos/anchor_resource/nomal/610474_0_0_1.mp4', 'http://res.oyechat.club/videos/robot_resource/nomal/cover/610474_0_0_1.jpg', 'video', 0, 0);
INSERT INTO `hwsj`.`cmf_anchor_resource`(`uid`, `url`, `cover`, `type`, `tag`, `use_type`) VALUES ('696780', 'http://res.oyechat.club/videos/anchor_resource/nomal/696780_0_0_1.mp4', 'http://res.oyechat.club/videos/robot_resource/nomal/cover/696780_0_0_1.jpg', 'video', 0, 0);
INSERT INTO `hwsj`.`cmf_anchor_resource`(`uid`, `url`, `cover`, `type`, `tag`, `use_type`) VALUES ('310840', 'http://res.oyechat.club/videos/anchor_resource/nomal/310840_0_0_1.mp4', 'http://res.oyechat.club/videos/robot_resource/nomal/cover/310840_0_0_1.jpg', 'video', 0, 0);
INSERT INTO `hwsj`.`cmf_anchor_resource`(`uid`, `url`, `cover`, `type`, `tag`, `use_type`) VALUES ('333376', 'http://res.oyechat.club/videos/anchor_resource/nomal/333376_0_0_1.mp4', 'http://res.oyechat.club/videos/robot_resource/nomal/cover/333376_0_0_1.jpg', 'video', 0, 0);
INSERT INTO `hwsj`.`cmf_anchor_resource`(`uid`, `url`, `cover`, `type`, `tag`, `use_type`) VALUES ('696877', 'http://res.oyechat.club/videos/anchor_resource/nomal/696877_0_0_1.mp4', 'http://res.oyechat.club/videos/robot_resource/nomal/cover/696877_0_0_1.jpg', 'video', 0, 0);
INSERT INTO `hwsj`.`cmf_anchor_resource`(`uid`, `url`, `cover`, `type`, `tag`, `use_type`) VALUES ('676418', 'http://res.oyechat.club/videos/anchor_resource/nomal/676418_0_0_1.mp4', 'http://res.oyechat.club/videos/robot_resource/nomal/cover/676418_0_0_1.jpg', 'video', 0, 0);
INSERT INTO `hwsj`.`cmf_anchor_resource`(`uid`, `url`, `cover`, `type`, `tag`, `use_type`) VALUES ('252456', 'http://res.oyechat.club/videos/anchor_resource/nomal/252456_0_0_1.mp4', 'http://res.oyechat.club/videos/robot_resource/nomal/cover/252456_0_0_1.jpg', 'video', 0, 0);
INSERT INTO `hwsj`.`cmf_anchor_resource`(`uid`, `url`, `cover`, `type`, `tag`, `use_type`) VALUES ('677053', 'http://res.oyechat.club/videos/anchor_resource/nomal/677053_0_0_1.mp4', 'http://res.oyechat.club/videos/robot_resource/nomal/cover/677053_0_0_1.jpg', 'video', 0, 0);
INSERT INTO `hwsj`.`cmf_anchor_resource`(`uid`, `url`, `cover`, `type`, `tag`, `use_type`) VALUES ('624331', 'http://res.oyechat.club/videos/anchor_resource/nomal/624331_0_0_1.mp4', 'http://res.oyechat.club/videos/robot_resource/nomal/cover/624331_0_0_1.jpg', 'video', 0, 0);
INSERT INTO `hwsj`.`cmf_anchor_resource`(`uid`, `url`, `cover`, `type`, `tag`, `use_type`) VALUES ('343677', 'http://res.oyechat.club/videos/anchor_resource/nomal/343677_0_0_1.mp4', 'http://res.oyechat.club/videos/robot_resource/nomal/cover/343677_0_0_1.jpg', 'video', 0, 0);