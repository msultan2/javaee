/*
    System: BlueTruth Outstation
    Language/Build: MS VC 2008 / Linux GCC 4.2+
    Description: This class has been initially created by The Late Miao Yu R.I.P.
        for Emergency Telephone Software and later used for other projects:
        the NECT and the IPT where it was maintained by Radoslaw Golebiewski.
        The CircularBuffer class is used to store values and retrieve
        old values in a circular manner.

    Modification History:

    Date        Who     SCJS No     Remarks
    24/10/2013  RG      001         V1.00 First Issue
*/

#ifndef _CIRCULAR_BUFFER_H_
#define _CIRCULAR_BUFFER_H_

#include "types.h"
#include "logger.h"

#include <vector>
#include <stddef.h>
#include <sstream>
#include <cassert>


namespace Model
{

template <class Type>
class CircularBuffer
{

public:

    //! constructor
    explicit CircularBuffer(const size_t size=0);

    //! destructor
    virtual ~CircularBuffer();

    //! get delayed sample. This sample is taken from the end of the buffer
    Type getDelayed(const size_t delay) const;

    //! get delayed sample pointer. This sample is taken from the end of the buffer
    const Type* getDelayedPtr(const size_t delay, size_t& index) const;

    //! store input at the beginning of the buffer, move index to a newer sample.
    //! All older samples get removed.
    void process(const Type &input);

    //! change size of the buffer
    void setSize(const size_t size);

    //! get size of the buffer
    size_t getSize() const { return m_size; }

    //! get current index, i.e. show where you are in the buffer
    size_t getCurrentIndex() const { return m_currentIndex; }

    //! reset the circular buffer to its initial state
    void reset();

    //! Fill the buffer with a value
    void fill(const Type value);

private:

    //! copy constructor. Not implemented
    CircularBuffer(const CircularBuffer& rhs);
    //! copy assignment operator. Not implemented
    CircularBuffer& operator=(const CircularBuffer& rhs);

    //! clears the circular buffer
    void clear();

    //! a buffer where the values are stored
    Type* m_bufferPtr;

    //! buffer size
    size_t m_size;

    //! temporary variable used to reduce read time from the vector
    size_t m_size_minus_one;

    //! current position within the buffer
    size_t m_currentIndex;

    //! temporary variable used to reduce read time from the vector
    size_t m_size_minus_one_plus_currentIndex;
};

template <class Type>
CircularBuffer<Type>::CircularBuffer(const size_t size) :
    m_bufferPtr(0),
    m_size(size),
    m_size_minus_one(size - 1),
    m_currentIndex(0),
    m_size_minus_one_plus_currentIndex(m_size - 1 + m_currentIndex)
{
    m_bufferPtr = new Type[size];

    clear();
}

template <class Type>
CircularBuffer<Type>::~CircularBuffer()
{
    delete[] m_bufferPtr;
}

template <class Type>
inline const Type* CircularBuffer<Type>::getDelayedPtr(const size_t delay, size_t& index) const
{
#if !defined DISABLE_ASSERTIONS
    assert(delay < m_size);
#endif

    //The division or modulo operation is very inefficient on TI so branching should be applied here
    index = m_size_minus_one_plus_currentIndex - delay;
    if (index >= m_size)
    {
        index -= m_size;
#if !defined DISABLE_ASSERTIONS
        assert(index < m_size);
#endif
    }
    //else do nothing

    const Type* result = &m_bufferPtr[index];

    return result;
}

template <class Type>
inline Type CircularBuffer<Type>::getDelayed(const size_t delay) const
{
#if !defined DISABLE_ASSERTIONS
    assert(delay < m_size);
#endif

    //The division or modulo operation is very inefficient on TI so branching should be applied here
    size_t index = m_size_minus_one_plus_currentIndex - delay;
    if (index >= m_size)
    {
        index -= m_size;
#if !defined DISABLE_ASSERTIONS
        assert(index < m_size);
#endif
    }
    //else do nothing

    Type result = m_bufferPtr[index];

    return result;
}

template <class Type>
inline void CircularBuffer<Type>::process(const Type &input)
{
#if !defined DISABLE_ASSERTIONS
    assert(m_currentIndex < m_size);
#endif

    m_bufferPtr[m_currentIndex++] = input;

    //The division or modulo operation is very inefficient on TI so branching should be applied here
    if (m_currentIndex >= m_size)
    {
        m_currentIndex -= m_size;
#if !defined DISABLE_ASSERTIONS
        assert(m_currentIndex < m_size);
#endif
    }
    //else do nothing

    m_size_minus_one_plus_currentIndex = m_size_minus_one + m_currentIndex;
}

template <class Type>
void CircularBuffer<Type>::setSize(const size_t size)
{
    if (m_bufferPtr != 0)
    {
        delete [] m_bufferPtr;
        m_bufferPtr = 0;
    }
    //else do nothing

    m_bufferPtr = new Type[size];

    m_size = size;
    m_size_minus_one = size - 1;
    m_currentIndex = 0;
    m_size_minus_one_plus_currentIndex = m_size - 1 + m_currentIndex;

    clear();
}

template <class Type>
void CircularBuffer<Type>::clear()
{
    const Type INITIAL_VALUE = static_cast<Type>(0);

    for(size_t i=0; i<m_size; ++i)
    {
        m_bufferPtr[i] = INITIAL_VALUE;
    }
}

template <class Type>
void CircularBuffer<Type>::fill(const Type value)
{
    for(size_t i=0; i<m_size; ++i)
    {
        m_bufferPtr[i] = value;
    }
}

template <class Type>
void CircularBuffer<Type>::reset()
{
    clear();
    m_currentIndex = 0;
    m_size_minus_one_plus_currentIndex = m_size - 1 + m_currentIndex;
}

} //namespace

#endif // _CIRCULAR_BUFFER_H_
