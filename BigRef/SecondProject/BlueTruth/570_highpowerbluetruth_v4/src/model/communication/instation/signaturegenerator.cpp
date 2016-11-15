#include "stdafx.h"
#include "signaturegenerator.h"

namespace
{
    const uint32_t A = 48271U;
    const uint32_t M = 2147483647U; //should be 2^31-1
}

namespace InStation
{

SignatureGenerator::SignatureGenerator()
:
ISignatureGenerator(),
m_x(1),
m_A(A),
m_M(M)
{
    //do nothing
}

SignatureGenerator::~SignatureGenerator()
{

}

uint32_t SignatureGenerator::getNewSignature()
{
    m_x = (m_A * m_x) % m_M;
    return m_x;
}

void SignatureGenerator::setSeed(const uint32_t value)
{
    m_x = value;
}

}
