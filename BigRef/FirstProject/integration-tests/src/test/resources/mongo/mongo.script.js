if (rs.isMaster().ismaster) {

    var usersMap = {};

    var usersCursor = db.user.find();

    usersCursor.forEach(function (item) {
        usersMap[item._id] = item
    });

    var dbRefForTokenUser = ObjectId("575ec77916323de1c1cf07ef");
    var dbRefForRoberto = "5172c56d68881249fbf88259";
    var dbRefForVictor = "5772856d68481249fbf66327";
    var dbRefForUserGroup1 = ObjectId("5772856d68481249fbf66000");
    var dbRefForUserGroup2 = ObjectId("5772856d68481249fbf66111");
    var dbRefForUserGroup3 = ObjectId("5772856d68481249fbf66987");
    var dbRefForUserGroup4 = ObjectId("5772856d68481249fbf66988");
    var dbRefForUserGroup5 = ObjectId("5772856d68481249fbf66989");
    var dbRefForUserGroup6 = ObjectId("5772856d68481249fbf66990");
    var dbRefForUserGroup7 = ObjectId("5772856d68481249fbf87654");
    var dbRefForUserGroup8 = ObjectId("5772856d68481249fb876543");
    var dbRefForAccountClassifications1 = ObjectId("57add29fb902a301eb92d42e");
    var dbRefForAccountClassifications2 = ObjectId("57add29fb902a301eb92d42f");

    var user1 = {
        "name": "Sergio",
        "address": "Calle Rio Ebro",
        "email": "sergio@ssl.com",
        "primaryPhone": "123456789",
        "mobile": "07777777777",
        "employer": "SSL",
        "mcr": "ABC",
        "rcc": "West Midlands",
        "projectSponsor": "Roberto",
        "roles": ["USER"],
        "passwordHash": "$2a$13$0YtHETbtCea8YCNA8DHBu.IUIKcAquKXFDa2ZoNRw9MRt3nWK9V2S", //ssl1324
        "passwordChangedDate": ISODate(),
        "enabled": true,
        "userGroup": DBRef("userGroup", dbRefForUserGroup1),
        "lastSuccessfulLogin": ISODate(),
        "accountNonLocked" : true
    };

    var user2 = {
        "_id": dbRefForTokenUser,
        "name": "Merce",
        "address": "Calle Rio Ebro",
        "email": "merce@ssl.com",
        "primaryPhone": "123456789",
        "mobile": "07777777777",
        "employer": "SSL",
        "mcr": "ABC",
        "rcc": "West Midlands",
        "projectSponsor": "Roberto",
        "roles": ["USER"],
        "passwordHash": "$2a$13$0YtHETbtCea8YCNA8DHBu.IUIKcAquKXFDa2ZoNRw9MRt3nWK9V2S", //ssl1324
        "passwordChangedDate": ISODate(),
        "enabled": true,
        "userGroup": DBRef("userGroup", dbRefForUserGroup1),
        "lastSuccessfulLogin": ISODate(),
        "accountNonLocked" : true
    };

    var user3 = {
        "_id": ObjectId(dbRefForRoberto),
        "name": "Roberto",
        "address": "Calle San Isidro",
        "email": "roberto@ssl.com",
        "primaryPhone": "123456789",
        "mobile": "07777777777",
        "employer": "SSL",
        "mcr": "ABC",
        "rcc": "East Midlands",
        "projectSponsor": "Victor",
        "roles": ["HEAPPROVER"],
        "passwordHash": "$2a$13$0YtHETbtCea8YCNA8DHBu.IUIKcAquKXFDa2ZoNRw9MRt3nWK9V2S", //ssl1324
        "passwordChangedDate": ISODate(),
        "enabled": true,
        "userGroup": DBRef("userGroup", dbRefForUserGroup2),
        "twoFactorAuthenticationSecret": "LZEX2HR3IXYCB3HPVLQSB4QB5Q",
        "lastSuccessfulLogin": ISODate(),
        "accountNonLocked" : true
    };

    var user4 = {
        "name": "Rocio",
        "address": "Avenida Paraiso",
        "email": "rocio@ssl.com",
        "primaryPhone": "123456789",
        "mobile": "07777777777",
        "employer": "SSL",
        "mcr": "ABC",
        "rcc": "North West",
        "projectSponsor": "Roberto",
        "roles": ["HEAPPROVER"],
        "passwordHash": "$2a$13$0YtHETbtCea8YCNA8DHBu.IUIKcAquKXFDa2ZoNRw9MRt3nWK9V2S", //ssl1324
        "passwordChangedDate": ISODate(),
        "enabled": true,
        "userGroup": DBRef("userGroup", dbRefForUserGroup2),
        "lastSuccessfulLogin": ISODate("2016-01-14T00:00:00Z"),
        "accountNonLocked" : true
    };

    var user5 = {
        "name": "Victor",
        "address": "Calle Rio Guadarrama",
        "email": "victor@ssl.com",
        "primaryPhone": "123456789",
        "mobile": "07777777777",
        "employer": "SSL",
        "mcr": "ABC",
        "rcc": "North East",
        "projectSponsor": "Roberto",
        "roles": ["SERVICEMANAGER"],
        "passwordHash": "$2a$13$0YtHETbtCea8YCNA8DHBu.IUIKcAquKXFDa2ZoNRw9MRt3nWK9V2S", //ssl1324
        "passwordChangedDate": ISODate(),
        "enabled": true,
        "userGroup": DBRef("userGroup", dbRefForUserGroup2),
        "lastSuccessfulLogin": ISODate(),
        "accountNonLocked" : true
    };

    var user6 = {
        "name": "Sara",
        "address": "Calle Carrera",
        "email": "sara@ssl.com",
        "primaryPhone": "123456789",
        "mobile": "07777777777",
        "employer": "SSL",
        "mcr": "ABC",
        "rcc": "East",
        "projectSponsor": "Rocio",
        "roles": ["HELPDESK"],
        "passwordHash": "$2a$13$0YtHETbtCea8YCNA8DHBu.IUIKcAquKXFDa2ZoNRw9MRt3nWK9V2S", //ssl1324
        "passwordChangedDate": ISODate(),
        "enabled": true,
        "userGroup": DBRef("userGroup", dbRefForUserGroup2),
        "lastSuccessfulLogin": ISODate(),
        "accountNonLocked" : true
    };

    var user7 = {
        "name": "User for 2FA",
        "address": "a\nmulti\nline\naddress",
        "email": "some.user@costain.com",
        "primaryPhone": "123456789",
        "mobile": "07777777777",
        "employer": "Costain",
        "mcr": "East",
        "rcc": "East",
        "projectSponsor": "Rocio",
        "roles": ["USER"],
        "passwordHash": "$2a$13$0YtHETbtCea8YCNA8DHBu.IUIKcAquKXFDa2ZoNRw9MRt3nWK9V2S", //ssl1324
        "passwordChangedDate": ISODate(),
        "enabled": true,
        "userGroup": DBRef("userGroup", dbRefForUserGroup2),
        "lastSuccessfulLogin": ISODate(),
        "accountNonLocked" : true
    };

    var user8 = {
        "name": "Existing 2FA User",
        "address": "a\nmulti\nline\naddress",
        "email": "another.user@costain.com",
        "primaryPhone": "123456789",
        "mobile": "07777777777",
        "employer": "Costain",
        "mcr": "East",
        "rcc": "East",
        "projectSponsor": "Rocio",
        "roles": ["USER"],
        "passwordHash": "$2a$13$0YtHETbtCea8YCNA8DHBu.IUIKcAquKXFDa2ZoNRw9MRt3nWK9V2S", //ssl1324
        "passwordChangedDate": ISODate(),
        "enabled": true,
        "twoFactorAuthenticationSecret": "LZEX2HR3IXYCB3HPVLQSB4QB5Q",
        "userGroup": DBRef("userGroup", dbRefForUserGroup2),
        "lastSuccessfulLogin": ISODate(),
        "accountNonLocked" : true
    };

    var user9 = {
        "name": "Johnny Bravo",
        "address": "Calle Peineta",
        "email": "johnny@ssl.com",
        "primaryPhone": "123456789",
        "mobile": "07777777777",
        "employer": "SSL",
        "mcr": "ABC",
        "rcc": "East",
        "projectSponsor": "Rocio",
        "roles": ["USER"],
        "passwordHash": "$2a$13$0YtHETbtCea8YCNA8DHBu.IUIKcAquKXFDa2ZoNRw9MRt3nWK9V2S", //ssl1324
        "passwordChangedDate": ISODate("2016-01-14T00:00:00Z"),
        "enabled": true,
        "userGroup": DBRef("userGroup", dbRefForUserGroup2),
        "lastSuccessfulLogin": ISODate(),
        "accountNonLocked" : true
    };
    
    var user10 = {
        "name": "Existing 2FA User With Expired Password",
        "address": "a\nmulti\nline\naddress",
        "email": "expiredpass@costain.com",
        "primaryPhone": "123456789",
        "mobile": "07777777777",
        "employer": "Costain",
        "mcr": "East",
        "rcc": "East",
        "projectSponsor": "Rocio",
        "roles": ["USER"],
        "passwordHash": "$2a$13$0YtHETbtCea8YCNA8DHBu.IUIKcAquKXFDa2ZoNRw9MRt3nWK9V2S", //ssl1324
        "passwordChangedDate": ISODate("2016-01-14T00:00:00Z"),
        "enabled": true,
        "userGroup": DBRef("userGroup", dbRefForUserGroup2),
        "twoFactorAuthenticationSecret": "LZEX2HR3IXYCB3HPVLQSB4QB5Q",
        "lastSuccessfulLogin": ISODate(),
        "accountNonLocked" : true
    };
    
    var user11 = {
        "name": "Test user for last login date",
        "address": "another\nmulti\nline\naddress",
        "email": "james@ssl.com",
        "primaryPhone": "123456789",
        "mobile": "07777777777",
        "employer": "Costain",
        "mcr": "East",
        "rcc": "East",
        "projectSponsor": "Rocio",
        "roles": ["USER"],
        "passwordHash": "$2a$13$0YtHETbtCea8YCNA8DHBu.IUIKcAquKXFDa2ZoNRw9MRt3nWK9V2S", //ssl1324
        "passwordChangedDate": ISODate(),
        "enabled": true,
        "userGroup": DBRef("userGroup", dbRefForUserGroup2),
        "accountNonLocked" : true
    };

    var user12 = {
        "name": "User for account lockout",
        "address": "Calle Rio Ebro",
        "email": "jim.manico@ssl.com",
        "primaryPhone": "123456789",
        "mobile": "07777777777",
        "employer": "SSL",
        "mcr": "ABC",
        "rcc": "West Midlands",
        "projectSponsor": "Roberto",
        "roles": ["USER"],
        "passwordHash": "$2a$13$0YtHETbtCea8YCNA8DHBu.IUIKcAquKXFDa2ZoNRw9MRt3nWK9V2S", //ssl1324
        "passwordChangedDate": ISODate(),
        "enabled": true,
        "userGroup": DBRef("userGroup", dbRefForUserGroup1),
        "lastSuccessfulLogin": ISODate(),
        "accountNonLocked" : true
    };

    var user13 = {
        "name": "user to test re-enabled group",
        "address": "a\nmulti\nline\naddress",
        "email": "user@toTest.reEnabledGroup",
        "primaryPhone": "123456789",
        "mobile": "07777777777",
        "employer": "Costain",
        "mcr": "East",
        "rcc": "East",
        "projectSponsor": "Rocio",
        "roles": ["USER"],
        "passwordHash": "$2a$13$0YtHETbtCea8YCNA8DHBu.IUIKcAquKXFDa2ZoNRw9MRt3nWK9V2S", //ssl1324
        "passwordChangedDate": ISODate(),
        "enabled": true,
        "userGroup": DBRef("userGroup", dbRefForUserGroup6),
        "lastSuccessfulLogin": ISODate(),
        "accountNonLocked" : true
    };

    var user14 = {
        "name": "user to test suspended group",
        "address": "a\nmulti\nline\naddress",
        "email": "user@toTest.suspendedGroup",
        "primaryPhone": "123456789",
        "mobile": "07777777777",
        "employer": "Costain",
        "mcr": "East",
        "rcc": "East",
        "projectSponsor": "Rocio",
        "roles": ["USER"],
        "passwordHash": "$2a$13$0YtHETbtCea8YCNA8DHBu.IUIKcAquKXFDa2ZoNRw9MRt3nWK9V2S", //ssl1324
        "passwordChangedDate": ISODate(),
        "enabled": true,
        "userGroup": DBRef("userGroup", dbRefForUserGroup5),
        "lastSuccessfulLogin": ISODate(),
        "accountNonLocked" : true
    };

    var user15 = {
        "name": "user to test deleted user group",
        "address": "a\nmulti\nline\naddress",
        "email": "user@toTest.deletedUserGroup",
        "primaryPhone": "123456789",
        "mobile": "07777777777",
        "employer": "Costain",
        "mcr": "East",
        "rcc": "East",
        "projectSponsor": "Roberto",
        "roles": ["USER"],
        "passwordHash": "$2a$13$0YtHETbtCea8YCNA8DHBu.IUIKcAquKXFDa2ZoNRw9MRt3nWK9V2S", //ssl1324
        "passwordChangedDate": ISODate(),
        "enabled": true,
        "userGroup": DBRef("userGroup", dbRefForUserGroup7),
        "lastSuccessfulLogin": ISODate(),
        "accountNonLocked" : true
    };

    var newUsersItems = [
        user1,
        user2,
        user3,
        user4,
        user5,
        user6,
        user7,
        user8,
        user9,
        user10,
        user11,
        user12,
        user13,
        user14,
        user15
    ];

    var userGroup1 = {
        "_id": dbRefForUserGroup1,
        "groupName": "Kent Users",
        "deviceFilter":
                {
                    "rccRegion": "South West"
                },
        "accountClassification": DBRef("accountClassification", dbRefForAccountClassifications1),
        "status": "ENABLED"
    };

    var userGroup2 = {
        "_id": dbRefForUserGroup2,
        "groupName": "Yatton Users",
        "deviceFilter": {},
        "accountClassification": DBRef("accountClassification", dbRefForAccountClassifications2),
        "status": "ENABLED"
    };

    var userGroup3 = {
        "_id": dbRefForUserGroup3,
        "groupName": "Enabled group to be suspended",
        "deviceFilter": {},
        "accountClassification": DBRef("accountClassification", dbRefForAccountClassifications1),
        "status": "ENABLED"
    };

    var userGroup4 = {
        "_id": dbRefForUserGroup4,
        "groupName": "Suspended group to be enabled",
        "deviceFilter": {},
        "accountClassification": DBRef("accountClassification", dbRefForAccountClassifications1),
        "status": "SUSPENDED"
    };

    var userGroup5 = {
        "_id": dbRefForUserGroup5,
        "groupName": "Enabled group to be suspended with user to login",
        "deviceFilter": {},
        "accountClassification": DBRef("accountClassification", dbRefForAccountClassifications1),
        "status": "ENABLED"
    };

    var userGroup6 = {
        "_id": dbRefForUserGroup6,
        "groupName": "Suspended group to be enabled with user to login",
        "deviceFilter": {},
        "accountClassification": DBRef("accountClassification", dbRefForAccountClassifications1),
        "status": "SUSPENDED"
    };

    var userGroup7 = {
        "_id": dbRefForUserGroup7,
        "groupName": "User group with users to delete",
        "deviceFilter": {},
        "accountClassification": DBRef("accountClassification", dbRefForAccountClassifications2),
        "status": "ENABLED"
    };

    var userGroup8 = {
        "_id": dbRefForUserGroup8,
        "groupName": "User group without users to delete",
        "deviceFilter": {},
        "accountClassification": DBRef("accountClassification", dbRefForAccountClassifications1),
        "status": "ENABLED"
    };

    var newUserGroups = [
        userGroup1,
        userGroup2,
        userGroup3,
        userGroup4,
        userGroup5,
        userGroup6,
        userGroup7,
        userGroup8
    ];

    var userReg1 = {
        "name": "Sergio",
        "address": "Calle Rio Ebro",
        "email": "sergio@ssl.com",
        "primaryPhone": "123456789",
        "mobile": "07777777777",
        "employer": "SSL",
        "mcr": "ABC",
        "rcc": "North West",
        "projectSponsor": dbRefForRoberto,
        "requestStatus": "APPROVED",
        "accessRequestReason": "I needed",
        "accessRequired": "SSL",
        "tandcAccepted": true
    };
    var userReg2 = {
        "name": "Merce",
        "address": "Calle Rio Ebro",
        "email": "merce@ssl.com",
        "primaryPhone": "123456789",
        "mobile": "07777777777",
        "employer": "SSL",
        "mcr": "ABC",
        "rcc": "South East",
        "projectSponsor": dbRefForRoberto,
        "requestStatus": "PENDING",
        "accessRequestReason": "My supervisor wants me to have an account",
        "accessRequired": "South London speed cameras",
        "tandcAccepted": true
    };
    var userReg3 = {
        "name": "Roberto",
        "address": "Calle San Isidro",
        "email": "roberto@ssl.com",
        "primaryPhone": "123456789",
        "mobile": "07777777777",
        "employer": "SSL",
        "mcr": "ABC",
        "rcc": "East Midlands",
        "projectSponsor": dbRefForVictor,
        "requestStatus": "PENDING",
        "accessRequestReason": "I need to include some devices in RMAS",
        "accessRequired": "North Bristol SOS phones",
        "tandcAccepted": true
    };
    var userReg4 = {
        "name": "Rocio",
        "address": "Avenida Paraiso",
        "email": "rocio@ssl.com",
        "primaryPhone": "123456789",
        "mobile": "07777777777",
        "employer": "SSL",
        "mcr": "ABC",
        "rcc": "East Midlands",
        "projectSponsor": dbRefForRoberto,
        "requestStatus": "PENDING",
        "accessRequestReason": "I want to update some devices",
        "accessRequired": "Any company",
        "tandcAccepted": true
    };

    var userReg5 = {
        "name": "Victor",
        "address": "Calle Rio Guadarrama",
        "email": "victor@ssl.com",
        "primaryPhone": "123456789",
        "mobile": "07777777777",
        "employer": "SSL",
        "mcr": "ABC",
        "rcc": "East",
        "projectSponsor": dbRefForRoberto,
        "requestStatus": "REJECTED",
        "accessRequestReason": "I would like to review RMAS users",
        "accessRequired": "SSL",
        "tandcAccepted": true
    };

    var newUserRegistrationsItems = [
        userReg1,
        userReg2,
        userReg3,
        userReg4,
        userReg5
    ];

    var newDevices = [
        {
            "_id": "10.163.49.68",
            "maintenanceContractRegion": "South West",
            "rccRegion": "South West",
            "enrolmentDate": ISODate(),
            "bandwidthLimit": NumberInt(256),
            "manufacturer": "SSL",
            "manufacturerType": "typ123",
            "serialNumber": "ser123",
            "hardwareVersion": "har123",
            "firmwareVersion": "fir123",
            "hostname": "M1-4567A1.ami.ha.org",
            "haGeographicAddress": "M1-4567A1",
            "latitude": 53.050274,
            "longitude": -2.191667,
            "deviceList": {"ami": ["signal450enforcement"]},
            "manufacturerSpecificData": "---",
            "status": "OK"
        },
        {
            "_id": "1.1.1.68",
            "maintenanceContractRegion": "North West",
            "rccRegion": "South West",
            "enrolmentDate": ISODate(),
            "bandwidthLimit": NumberInt(256),
            "manufacturer": "SSL",
            "manufacturerType": "typ123",
            "serialNumber": "ser123",
            "hardwareVersion": "har123",
            "firmwareVersion": "fir123",
            "hostname": "M1-4567A1.ami.ha.org",
            "haGeographicAddress": "M1-4567A1",
            "latitude": 53.050274,
            "longitude": -2.191667,
            "deviceList": {"ami": ["signal450enforcement"]},
            "manufacturerSpecificData": "---",
            "status": "OK"
        },
        {
            "_id": "1.1.1.1",
            "maintenanceContractRegion": "South West",
            "rccRegion": "South East",
            "enrolmentDate": ISODate(),
            "bandwidthLimit": NumberInt(256),
            "manufacturer": "Manufacturer 2",
            "manufacturerType": "typ456",
            "serialNumber": "ser456",
            "hardwareVersion": "har456",
            "firmwareVersion": "fir456",
            "hostname": "M1-4567A2.messagesign.ha.org",
            "haGeographicAddress": "M1-4567A2",
            "latitude": 54.050271,
            "longitude": -2.375008,
            "manufacturerSpecificData": "---",
            "deviceList": {
                "alm": [],
                "ami": [
                    "signal94xxstandard",
                    "signal94xxstandard",
                    "signal94xxstandard",
                    "signal94xxstandard"
                ]
            },
            "status": "OK"
        },
        {
            "_id": "1.1.1.2",
            "maintenanceContractRegion": "South West",
            "rccRegion": "East",
            "enrolmentDate": ISODate(),
            "bandwidthLimit": NumberInt(256),
            "manufacturer": "Manufacturer 2",
            "manufacturerType": "typ789",
            "serialNumber": "ser789",
            "hardwareVersion": "har789",
            "firmwareVersion": "fir789",
            "hostname": "M1-4567B1.signal.ha.org",
            "haGeographicAddress": "M1-4567B1",
            "latitude": 53.048609,
            "longitude": -1.385563,
            "deviceList": {"signal": ["input320amplifier", "input420amplifier", "input520amplifier"], "hadecsos": ["input320amplifier"]},
            "manufacturerSpecificData": "---",
            "status": "OK"
        },
        {
            "_id": "1.1.1.3",
            "maintenanceContractRegion": "South West",
            "rccRegion": "North East",
            "enrolmentDate": ISODate(),
            "bandwidthLimit": NumberInt(256),
            "manufacturer": "Manufacturer 2",
            "manufacturerType": "typ012",
            "serialNumber": "ser012",
            "hardwareVersion": "har012",
            "firmwareVersion": "fir012",
            "hostname": "M1-4567B2.hadecsos.ha.org",
            "haGeographicAddress": "M1-4567B2",
            "latitude": 57.048610,
            "longitude": -2.385558,
            "deviceList": {"hadecsos": ["result440booster"]},
            "manufacturerSpecificData": "---",
            "status": "OK"
        },
        {
            "_id": "1.1.1.4",
            "maintenanceContractRegion": "Yorkshire and the Humber",
            "rccRegion": "North East",
            "enrolmentDate": ISODate(),
            "bandwidthLimit": NumberInt(256),
            "manufacturer": "Manufacturer 2",
            "manufacturerType": "typ345",
            "serialNumber": "ser345",
            "hardwareVersion": "har345",
            "firmwareVersion": "fir345",
            "hostname": "M1-4567K1.midas.ha.org",
            "haGeographicAddress": "M1-4567K1",
            "latitude": 51.048606,
            "longitude": -2.385566,
            "deviceList": {"midas": ["power580splitter"]},
            "manufacturerSpecificData": "---",
            "status": "OK"
        }
    ];

    var newRMASKeys = [
        {
            "generatedTimestamp": ISODate("2016-01-14T00:00:00Z"),
            "expiredTimestamp": ISODate(),
            "type": "PRIVATE",
            "algorithm": "ssh-rsa",
            "content": "-----BEGIN RSA PRIVATE KEY-----\r\nMIIEowIBAAKCAQEAnW6RlstSejKsvjd+gJjKrOf5+aTbVYOZgwhu7r8eCStmY2Fe\r\n5S5toRVv0a3brNcPB9lEbb+2oLVl2td7CrfQBnuzkQFC4l+9nXGeiE2/0D3jHVzb\r\ny37sEEBeR1nfmSIDWDb0Q80NBg3TzTQRqeVHmY6d0HOd4oVYptTfABpHmf+y5qus\r\ncd6B2K9TNA8+aP9m/VPyUMVZS2a2ESBnDEI3KQY46cuEcF/fYX77rrJSzf0Sgzza\r\nCrvrEYeJdlkaTOJi8aZk9IVRdwP76EFrucWJADpB+g771+Z3OFALKCPXgJM9mA/U\r\n3Bk/PM55vMfNaxzhV0dCqSnNzAOcyWTA3uVJnQIDAQABAoIBAA4MR8GE8x8kVnXC\r\nze77s1oqeQvlwgynaZNftUZol22KCeDkV2tr8SxmlUvIj8mkhxrUMF76tHkytpwI\r\njloufMmXUDaoPielE25xLQxMo8kZPKLqHlLSSI5KsB79zg4EEvhULwe9zGO16Chv\r\ndQQMCY+SQlLPfk8wRvDcHeoMbkc3ffu4tu8RG2xwWT2LCm+OxQYYpw4RsLBy8U7Z\r\nsuhxKQuoJzRoIfVxkN/h9Bn4ObTfXwXLcswmPcHQLghePQDoTe0Kb2WCggxpyHbG\r\nQePM7sbDabAHm/ZxgE9kWiReZVGCm25LNUXHHeAUCZ3ahn+nQ+B72CfA0m9Y8vef\r\nvYVfXaECgYEA16uV78H8TRLlNKFfQg2XT3KZMCoUaY00vrxDVAjOmXUWNpTO/V3Q\r\nglPKNbeZ4VFpc7/KMk07rkj3RSVKr+pWKx0iMYfmF2c0vLhev4t7TfQtuu5eMBfv\r\np+zBGxph2ciJFnaf8VEj2kkZinWYLNwCRSkWAD3aptRZdWnRnjMx+ocCgYEAut8J\r\ntr49gCyVsLNrWLKOEFMmC8AMZPfR7tkcCMOJQ31Ax/+QvGjoCf3St29gbLX2jscx\r\nJBSoE86beuy17b/R9cHwkftsHb4LRMQUBb5GnaSkfzDBblSP52uSuQo7cD14mfVe\r\n7EE5LJqrwVGCyRZhjBT9Zq7bI3U9fSYFxwsXr7sCgYAiP14c55oQBysck2+UBqJ7\r\nfEA1NMlvBXxVuYGbi5Z7KeuwGStRcp9Uwsd+hjxKWmo7dj4+hKMwhue1NDnK+5RZ\r\nXlP6t5DjMIFgYoqxMg4Pj/HfGFEeo/5rlR4JFsRpF/4k8gtt/6uI00jaAth6bylx\r\nIBgdS7U3/sqB6Z7e11RdawKBgQC07RUelXo+CuyzNEOOkPHy5E5Fuh9F8pqTQile\r\nYtMXQMHj4ZBkmr1uTw4hA/i7yEF7Y2g0ortI0hS/I0fRdJL3+lnNPTwkX8fRQHaS\r\nSPNd1fZeHvpos/7P9NsNxNZfiWSmy2aoH04X3XjFpwGVZ9HyKrBdJpaM8goijRmy\r\n6Q552QKBgGH+H1vxOmMMZPr12HngGQqAvfooWPfwQ9gf9u36OlfyaI51CUIvd5vc\r\njgagy3n8JZDOd/klgd4AFBSyFNLdx3+NxvMrQr/B/EeDl6SmBc3lgtt+tfBBWHIH\r\nl+o97zABBWYXk92r+T6UPcEDdiOQfizIZa3nAenk5a0Uw7Ehwn1L\r\n-----END RSA PRIVATE KEY-----"
        },
        {
            "generatedTimestamp": ISODate(),
            "expiredTimestamp": null,
            "type": "PRIVATE",
            "algorithm": "ssh-rsa",
            "content": "-----BEGIN RSA PRIVATE KEY-----\r\nMIIEowIBAAKCAQEAqPq7/U02n7xK23o4Os6P9BbO3DKUKBVS3r1z/hgc5mg320mi\r\nNUSJdPbatOE0Zw/AhEPqIHRXoMInS1xNC99v7vpNmcl9HEqslxuE35kdPM86eqjk\r\ntr2c/SYZfFLDm89QL8c/dfwQxkqUWDAIgC/CKeL0iLsVxdSjMSj1//MPv7Ubjjw+\r\nRuguEFT4cEN0CupfgVCsM54KGMqQs2eRuM9YSkeMy9Ks7ovzraunc+EMET4gbqNe\r\nymdIDPc0S2Xi396lceZwEyLc26b/zlvT/sw6xlCDGu4oMbKOZeKPjEZ+tOhDs6Se\r\nLlCMrcZJAfp2N5m8CI3iM4T3/UO1M4YGLD+czwIDAQABAoIBAFi3i4tpxowG56Zj\r\nfLIjUSDt+J1TUMXarC+VeBinwESX4vuWpTsiSKjIEft1YXXap1DJ91oiuv8D8G14\r\ngFsDbFkjwSpExxLbgdGz+QcL9HXAtws/MRVgZ9Pg9PB444oiRg9PpNnlBUmWTlwe\r\nm9fxOxeSuKAw2TWMdkGRJ9bBXjtfl4pOmqcWphGIFOj73SPmk4trYO9+NXkD57sq\r\nl8m2IchfmJJfTQW0CdwtL3VZWjGGhwm8RSMGQl1gTViL9hoPA06C2JWOrKLtbBUg\r\nC0iUDZXA/BQXC+v4OmH8OZG99Ro0El2G3bLi3JQPznqAWtFCTaMOVUrFMCLcc6/K\r\nn5IMTsECgYEA4SpsW3C8MgOH7+NLpbiKEsDZZ1iA4qv+yR3pk8qbkTqWkb1fdW1J\r\n1nRbb71eDXhM8lWp9cajJR/cDFh5ggmW9ncB4DeFkphl1zyGcFlIbN9Ry1+EuBaa\r\nri9bgxlIye+BYhl9EkfM5Rxx9L2ROnftgrUzXJTXSP99i3n22W/Isq0CgYEAwB6a\r\nVvq1QuDymk9MZjiiq36ViGQUAqWRnzxP9c24IFqeOXq4RAbQyhJGT2IqEtAsGgl5\r\nY4tFMo2dTu3LeSLnshfz7NOk5HhFv7vxmWOwhK2RXSmuXPolsmzC9bouhaEV1tEk\r\ni+MtSyO38tBpyf30A7d/e1+/CsW7Cxl10tMc+OsCgYB7Z1LHsruR9Zl1Ae+dvcv+\r\nhy8fksPgdz9GXlx7oVkteRy4dz9wDhufq1nJndGENGCHmCd0Cg+z8wgYxAykFjlk\r\njsi6Z/OO6nM2SXes72HgOWyMt6b3dXwE5vOSoDLiaZ9RcoK3mB5VmwdhiNulXcEQ\r\nF9s0bGapHU1jOc7uUHOAaQKBgQCuu6a4MTGm8Bz2h+T+m8gnneu2rv8cISY+44X+\r\nE+rBUmnR8XGiPWisQM696JRHpBpYXAGRrdtzzSUj4ldLh5mfevWZkizItzMI3ZEx\r\nZ1/mbE5noz84xTKAAzeWco1ZcFcCmBlbOXW6f3QQXcm5s0W6E6zYsw54Z7pkr/Vf\r\nmC8jnQKBgEcMDJJl6dFKjy5V+lZBamkNXUlVmjywnhc76QwKzGA0Qzg1QV0M5VT4\r\nYu1A8kuSYtAH73U80mr/25+Qm2z64ELVuHpXebxxEPHCDwfAIt3nsrHA+ecOL9sy\r\nnf05NXScYp8kGeZlnV3qTUtXHk9IcyssHCqQSWzWFX0T9D8vsVBA\r\n-----END RSA PRIVATE KEY-----"
        },
        {
            "generatedTimestamp": ISODate(),
            "expiredTimestamp": null,
            "type": "PUBLIC",
            "algorithm": "ssh-rsa",
            "content": "AAAAB3NzaC1yc2EAAAADAQABAAABAQDFojx1LxL5JNR3Vkd7Ea6SfCrnYFfXWwjus4seMjM9N1pLYGjtBZfL8xe2X8dAPldrJxF/mY0+jXQOOYcsZZ2+RXPDP5qsVNw0o1taW9yO2OC/v1JMWkKN0UmIhyBgvrN55wanmkxO/TWs2rn4c2IIto6mlpV04Is2RSclH2Ix9VIqrvL0belMFkR2kb7763IDs2TjJ9Bwo2ApvuGGe0RaWDIZUQbG+FPnid9lDUXMoEmGsMS7i4dBTFWPfpzc7W1+6W0AbfDRAWa/ZZwHKx9M9oAa9IFQBRNarZt2GjxUd2erMq/vwhSH2afKmqPXC4f2NdTVRe2KNbPw/Gac3pY7"
        }
    ];

    var newPasswordTokens = [
        {
            "_id": ObjectId("57681527848e820d2c30ced5"),
            "createdTimestamp": new ISODate(new Date(ISODate().getTime() - 1000 * 3600 * 24 * 15).toISOString()),
            "user": DBRef("user", dbRefForTokenUser)
        }
    ];

    var newAccountClassifications = [
        {
            "_id": dbRefForAccountClassifications1,
            "name": "Medium",
            "permissions": ["Roadside device log retrieval", "View roadside device status", "Roadside device firmware change", "Firmware upload to the RMAS store"]            
        },
        {
            "_id": dbRefForAccountClassifications2,
            "name": "Low",
            "permissions": ["Roadside device log retrieval", "View roadside device status", "Firmware upload to the RMAS store"]            
        }
    ];

    var permissions = [
        {"_id": "Roadside device log retrieval"},
        {"_id": "View roadside device status"},
        {"_id": "Roadside device firmware change"},
        {"_id": "Firmware upload to the RMAS store"}
    ];

    var rcc1 = {
        "_id": "East"
    };
    var rcc2 = {
        "_id": "East Midlands"
    };
    var rcc3 = {
        "_id": "West Midlands"
    };
    var rcc4 = {
        "_id": "North East"
    };
    var rcc5 = {
        "_id": "North West"
    };
    var rcc6 = {
        "_id": "South East"
    };
    var rcc7 = {
        "_id": "South West"
    };

    var rccs = [
        rcc1,
        rcc2,
        rcc3,
        rcc4,
        rcc5,
        rcc6,
        rcc7
    ];

    db.rmasKey.drop();
    db.user.drop();
    db.userRegistration.drop();
    db.device.drop();
    db.passwordResetToken.drop();
    db.userGroup.drop();
    db.accountClassification.drop();
    db.rcc.drop();
    db.permission.drop();

    db.rmasKey.insert(newRMASKeys);
    db.user.insert(newUsersItems);
    db.device.insert(newDevices);
    db.userRegistration.insert(newUserRegistrationsItems);
    db.passwordResetToken.insert(newPasswordTokens);
    db.userGroup.insert(newUserGroups);
    db.accountClassification.insert(newAccountClassifications);
    db.rcc.insert(rccs);
    db.permission.insert(permissions);

}
