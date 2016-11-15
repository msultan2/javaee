#include "stdafx.h"
#include <gtest/gtest.h>

#include "types.h"
#include "circularbuffer.h"

using Model::CircularBuffer;


namespace testing
{

TEST(CircularBuffer, ConstructorFromAddress)
{
    CircularBuffer<int> buffer(5);
    int32_t x = 0;

    ASSERT_EQ(5u, buffer.getSize());

    for( x=0; x<100; x+=1)
    {
        buffer.process(x);

        EXPECT_EQ(buffer.getDelayed(0), x);

        if (x>1)
        {
            EXPECT_EQ(buffer.getDelayed(1), x-1);
        }
        else
        {
            EXPECT_EQ(buffer.getDelayed(1), 0);
        }

        if (x>2)
        {
            EXPECT_EQ(buffer.getDelayed(2), x-2);
        }
        else
        {
            EXPECT_EQ(buffer.getDelayed(2), 0);
        }

        if (x>3)
        {
            EXPECT_EQ(buffer.getDelayed(3), x-3);
        }
        else
        {
            EXPECT_EQ(buffer.getDelayed(3), 0);
        }

        if (x>3)
        {
            EXPECT_EQ(buffer.getDelayed(4), x-4);
        }
        else
        {
            EXPECT_EQ(buffer.getDelayed(4), 0);
        }

    }
    x-=1;

    EXPECT_EQ(buffer.getDelayed(0), x);
    EXPECT_EQ(buffer.getDelayed(1), x-1);
    EXPECT_EQ(buffer.getDelayed(2), x-2);
    EXPECT_EQ(buffer.getDelayed(3), x-3);
    EXPECT_EQ(buffer.getDelayed(4), x-4);

    //EXPECT_FLOAT_EQ(buffer.getDelayed(5), 0.0F); //exception will be reported
}

} //namespace testing
