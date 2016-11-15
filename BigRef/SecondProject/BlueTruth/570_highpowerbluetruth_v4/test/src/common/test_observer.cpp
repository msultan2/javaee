#include "stdafx.h"
#include "test_observer.h"


namespace Testing
{

TestObserver::TestObserver()
:
::IObserver(),
m_indexCollection()
{
}

TestObserver::~TestObserver()
{
}

void TestObserver::notifyOfStateChange(::IObservable* , const int index)
{
    m_indexCollection.push_back(index);
}

std::vector<int>& TestObserver::getIndexCollection()
{
    return m_indexCollection;
}

}
