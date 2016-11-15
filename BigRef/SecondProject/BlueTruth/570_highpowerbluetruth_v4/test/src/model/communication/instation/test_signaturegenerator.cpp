#include "stdafx.h"
#include "test_signaturegenerator.h"


namespace Testing
{

TestSignatureGenerator::TestSignatureGenerator()
:
InStation::ISignatureGenerator(),
m_x(1)
{
    //do nothing
}

TestSignatureGenerator::~TestSignatureGenerator()
{

}

uint32_t TestSignatureGenerator::getNewSignature()
{
    return m_x;
}

void TestSignatureGenerator::setNewSignature(const uint32_t value)
{
    m_x = value;
}

void TestSignatureGenerator::setSeed(const uint32_t )
{
    //do nothing
}

}
