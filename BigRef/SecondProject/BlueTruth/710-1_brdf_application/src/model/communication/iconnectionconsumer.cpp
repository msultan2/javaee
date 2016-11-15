#include "stdafx.h"
#include "iconnectionconsumer.h"


namespace Model
{

IConnectionConsumer::IConnectionConsumer()
{

}

IConnectionConsumer::~IConnectionConsumer()
{

}

void IConnectionConsumer::onConnect(const bool )
{
    //do nothing
}

void IConnectionConsumer::onClose()
{
    //do nothing
}

void IConnectionConsumer::onBackoffTimerStarted()
{
    //do nothing
}

void IConnectionConsumer::onBackoffTimerExpired()
{
    //do nothing
}

} //namespace
