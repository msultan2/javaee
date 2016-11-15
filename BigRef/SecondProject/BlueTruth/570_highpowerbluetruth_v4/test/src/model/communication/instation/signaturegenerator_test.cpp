#include "stdafx.h"
#include <gtest/gtest.h>

#include "instation/signaturegenerator.h"


using InStation::SignatureGenerator;


TEST(SignatureGenerator, statisticalProperties)
{
    SignatureGenerator generator;
    generator.setSeed(13);

    uint64_t total = 0;
    uint64_t totalSquared = 0;
    const uint64_t I_MAX = 10000000;
    for (uint64_t i=0; i<I_MAX; ++i)
    {
        const uint64_t VALUE = generator.getNewSignature();
        total += VALUE;
        totalSquared += (VALUE * VALUE) >> 32U;
    }

    //Calculate average
    const uint64_t EX = total / I_MAX;
    const uint64_t EXPECTED_EX = 0x7FFFFFFFU/2;
    std::cout <<  "EX=" << EX << " (expected " << EXPECTED_EX << ")" << std::endl;
    EXPECT_NEAR(EXPECTED_EX, EX, EXPECTED_EX/100);

    //Calculate varance
    const uint64_t EX2 = (totalSquared / I_MAX);
    const uint64_t E2X = ((total / I_MAX) * (total / I_MAX)) >> 32;
    const uint64_t D2X = EX2 - E2X;
    const uint64_t EXPECTED_D2X = (((uint64_t)0x7FFFFFFFU * (uint64_t)0x7FFFFFFFU) >> 32)/12;
    std::cout << "D2X=" << D2X << "  (expected "
        << EXPECTED_D2X << ")" << std::endl;
    EXPECT_NEAR(EXPECTED_D2X,D2X, EXPECTED_D2X/10);
}

TEST(SignatureGenerator, setSeed)
{
    SignatureGenerator generator;
    generator.setSeed(17);

    const uint64_t VALUE_A1 = generator.getNewSignature();
    const uint64_t VALUE_A2 = generator.getNewSignature();

    generator.setSeed(17);
    const uint64_t VALUE_B1 = generator.getNewSignature();
    const uint64_t VALUE_B2 = generator.getNewSignature();

    EXPECT_EQ(VALUE_A1, VALUE_B1);
    EXPECT_EQ(VALUE_A2, VALUE_B2);
    EXPECT_NE(VALUE_A1, VALUE_A2);
}
