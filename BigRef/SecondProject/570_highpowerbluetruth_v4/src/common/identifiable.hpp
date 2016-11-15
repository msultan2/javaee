#include "identifiable.h"

#include <cassert>
#include <sstream>


template <class T>
Identifiable<T>::~Identifiable()
{
    //do nothing
}

template <class T>
Identifiable<T>::Identifiable()
:
m_identifierCollection(),
m_name()
{
    updateName();
}

template <class T>
Identifiable<T>::Identifiable(const T& id)
:
m_identifierCollection(),
m_name()
{
    m_identifierCollection.push_back(id);
    updateName();
}

template <class T>
Identifiable<T>::Identifiable(const Identifiable& rhs)
:
m_identifierCollection(rhs.m_identifierCollection),
m_name(rhs.m_name)
{
}

template <class T>
Identifiable<T>& Identifiable<T>::operator=(const Identifiable<T>& rhs)
{
    if (this != &rhs)
    {
        m_identifierCollection = rhs.m_identifierCollection;
        m_name = rhs.m_name;
    }
    //else do nothing

    return *this;
}


template <class T>
size_t Identifiable<T>::getNumberOfIdentifiers() const
{
    return m_identifierCollection.size();
}

template <class T>
bool Identifiable<T>::isOfIdentifier(const T& id) const
{
    bool result = false;

    for (typename TIdentifierCollection::const_iterator
            iter(m_identifierCollection.begin()), iterEnd(m_identifierCollection.end());
        iter != iterEnd;
        ++iter)
    {
        if (*iter == id)
        {
            result = true;
            break;
        }
        //else do nothing
    }

    return result;
}

template <class T>
void Identifiable<T>::addIdentifier(const T& id)
{
    assert(!isOfIdentifier(id));
    m_identifierCollection.push_back(id);
    updateName();
}

template <class T>
void Identifiable<T>::removeIdentifier(const T& id)
{
    assert(isOfIdentifier(id));

    for (typename TIdentifierCollection::iterator
            iter(m_identifierCollection.begin()), iterEnd(m_identifierCollection.end());
        iter != iterEnd;
        ++iter)
    {
        if (*iter == id)
        {
            m_identifierCollection.erase(iter);
            updateName();
            break;
        }
        //else do nothing
    }
}

template <class T>
const char* Identifiable<T>::getIdentifierName() const
{
    return m_name.c_str();
}

template <class T>
void Identifiable<T>::updateName()
{
    std::ostringstream ss;
    ss << "[";
    bool empty = true;

    for (typename TIdentifierCollection::iterator
            iter(m_identifierCollection.begin()), iterEnd(m_identifierCollection.end());
        iter != iterEnd;
        ++iter)
    {
        if (!empty)
        {
            ss << ",";
        }
        //else do nothing

        ss << *iter;
        empty = false;
    }
    ss << "]";

    m_name = ss.str();
}
