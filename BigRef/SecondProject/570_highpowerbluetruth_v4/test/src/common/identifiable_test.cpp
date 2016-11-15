#include "stdafx.h"
#include <gtest/gtest.h>

#include "identifiable.h"


TEST(Identifiable, constructor)
{
    const int ID1 = 1;
    const int ID2 = 2;
    Identifiable<int> object;

    EXPECT_EQ(0, object.getNumberOfIdentifiers());
    EXPECT_STREQ("[]", object.getIdentifierName());

    object.addIdentifier(ID1);
    EXPECT_EQ(1, object.getNumberOfIdentifiers());
    EXPECT_TRUE(object.isOfIdentifier(ID1));
    EXPECT_FALSE(object.isOfIdentifier(ID2));
    EXPECT_STREQ("[1]", object.getIdentifierName());

    object.addIdentifier(ID2);
    EXPECT_EQ(2, object.getNumberOfIdentifiers());
    EXPECT_TRUE(object.isOfIdentifier(ID1));
    EXPECT_TRUE(object.isOfIdentifier(ID2));
    EXPECT_STREQ("[1,2]", object.getIdentifierName());

    object.removeIdentifier(ID1);
    EXPECT_EQ(1, object.getNumberOfIdentifiers());
    EXPECT_FALSE(object.isOfIdentifier(ID1));
    EXPECT_TRUE(object.isOfIdentifier(ID2));
    EXPECT_STREQ("[2]", object.getIdentifierName());

    object.removeIdentifier(ID2);
    EXPECT_EQ(0, object.getNumberOfIdentifiers());
    EXPECT_FALSE(object.isOfIdentifier(ID1));
    EXPECT_FALSE(object.isOfIdentifier(ID2));
    EXPECT_STREQ("[]", object.getIdentifierName());
}

TEST(Identifiable, constructor_with_parameter)
{
    const int ID1 = 1;
    const int ID2 = 2;
    Identifiable<int> object(ID1);

    EXPECT_EQ(1, object.getNumberOfIdentifiers());
    EXPECT_TRUE(object.isOfIdentifier(ID1));
    EXPECT_FALSE(object.isOfIdentifier(ID2));
    EXPECT_STREQ("[1]", object.getIdentifierName());
}
