/*
    System: BlueTruth Raw Data Feed
    Language/Build: MS VC 2008 / Linux GCC 4.2+
*/

#ifndef _ATOMIC_VARIABLE
#define _ATOMIC_VARIABLE

#include <boost/thread.hpp>


/**
 * @brief A class that implements the pattern of an atomic variable using a mutex
 *
 * Example of use:
 * \code
    AtomicVariable<bool> value(true);
    if (value.get())
    {
        //do something
    }

    value.set(false);
   \endcode
 */
template<typename T>
class AtomicVariable
{
public:
    /**
     * @brief Constructor with the initial value
     * @param value initial value
     */
    AtomicVariable(const T& value)
    :
    m_value(value),
    m_accessMutex()
    {
        //do nothing
    }

    virtual ~AtomicVariable()
    {
        //do nothing
    }

    /**
     * @brief Set a value
     * @param value a value to be assigned to the atomic variable
     */
    void set(const T& value)
    {
        boost::lock_guard<boost::mutex> lock(m_accessMutex);
        m_value = value;
    }

    /**
     * @brief Get the current value
     * @return a value held in the atomic variable
     */
    T get() const
    {
        boost::lock_guard<boost::mutex> lock(m_accessMutex);
        return m_value;
    }

    /**
     * @brief Increase the value (prefix variant)
     */
    T& operator++()
    {
        boost::lock_guard<boost::mutex> lock(m_accessMutex);
        m_value++;
        return m_value;
    }

    /**
     * @brief Increase the value (postfix variant)
     */
    T operator++(int)
    {
        boost::lock_guard<boost::mutex> lock(m_accessMutex);
        T tmp(m_value);
        m_value++;

        return tmp;
    }

    /**
     * @brief Decrease the value (prefix variant)
     */
    T& operator--()
    {
        boost::lock_guard<boost::mutex> lock(m_accessMutex);
        m_value--;
        return m_value;
    }

    /**
     * @brief Decrease the value (postfix variant)
     */
    T operator--(int)
    {
        boost::lock_guard<boost::mutex> lock(m_accessMutex);
        T tmp(m_value);
        m_value--;

        return tmp;
    }

    /**
     * @brief Assign a value
     * @param value a value to be assigned to the atomic variable
     */
    T operator=(T value)
    {
        boost::lock_guard<boost::mutex> lock(m_accessMutex);
        m_value = value;
        return m_value;
    }

    /**
     * @brief Compare the current value agains another one
     * @param rhs a value to compare against
     * @return true if equal, false otherwise
     */
    inline bool operator==(const T& rhs)
    {
        boost::lock_guard<boost::mutex> lock(m_accessMutex);
        return (m_value == rhs);
    }

    /**
     * @brief Compare the current value agains another one
     * @param rhs a value to compare against
     * @return true if not equal, false otherwise
     */
    inline bool operator!=(const T& rhs)
    {
        boost::lock_guard<boost::mutex> lock(m_accessMutex);
        return (m_value != rhs);
    }

    /**
     * @brief Compare the current value agains another one
     * @param rhs a value to compare against
     * @return true if the current value is greater than the other one, false otherwise
     */
    inline bool operator> (const T& rhs)
    {
        boost::lock_guard<boost::mutex> lock(m_accessMutex);
        return (m_value > rhs);
    }

    /**
     * @brief Compare the current value agains another one
     * @param rhs a value to compare against
     * @return true if the current value is lower than the other one, false otherwise
     */
    inline bool operator< (const T& rhs)
    {
        boost::lock_guard<boost::mutex> lock(m_accessMutex);
        return (m_value < rhs);
    }

    /**
     * @brief Compare the current value agains another one
     * @param rhs a value to compare against
     * @return true if the current value is greater or equal than the other one, false otherwise
     */
    inline bool operator>= (const T& rhs)
    {
        boost::lock_guard<boost::mutex> lock(m_accessMutex);
        return (m_value >= rhs);
    }

    /**
     * @brief Compare the current value agains another one
     * @param rhs a value to compare against
     * @return true if the current value is lower or equal than the other one, false otherwise
     */
    inline bool operator<= (const T& rhs)
    {
        boost::lock_guard<boost::mutex> lock(m_accessMutex);
        return (m_value <= rhs);
    }


private:
    //! Default constructor - not implemented
    AtomicVariable();
    //! Copy constructor - not implemented
    AtomicVariable(const AtomicVariable &rhs);
    //! Assignment operator - not implemented
    AtomicVariable& operator=(const AtomicVariable &rhs);


    //Private members:
    T m_value;
    mutable boost::mutex m_accessMutex;

};

#endif //_ATOMIC_VARIABLE
