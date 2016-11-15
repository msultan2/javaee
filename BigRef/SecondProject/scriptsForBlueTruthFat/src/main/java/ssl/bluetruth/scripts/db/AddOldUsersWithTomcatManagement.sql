INSERT INTO instation_role VALUES ('manager-gui', 'Allows the user to use Tomcat Management.');

INSERT INTO instation_user VALUES ('santhosh v', 'santhosh', 'c20ad4d76fe97759aa27a0c99bff6710', 'SSL', NULL, true, 'santhoshv29.1987@gmail.com', NULL, 30, now());
INSERT INTO instation_user VALUES ('will', 'william', 'c20ad4d76fe97759aa27a0c99bff6710', 'SSL', NULL, true, 'test@gmail.com', NULL, 30, now());
INSERT INTO instation_user VALUES ('radek', 'radekg1000', 'f5efcc234dcc52e436362acf76a11a0a', 'SSL', NULL, true, 'as@qs.sa', NULL, 30, now());
  
-- Give all users all roles
DELETE FROM instation_user_role;
INSERT INTO instation_user_role SELECT instation_user.username, instation_role.role_name FROM instation_user, instation_role;

-- Put all users in all groups
DELETE FROM instation_user_logical_group;
INSERT INTO instation_user_logical_group SELECT instation_user.username, 'Test Group' FROM instation_user;
INSERT INTO instation_user_logical_group SELECT instation_user.username, 'GroupTestingDiagnostic' FROM instation_user;