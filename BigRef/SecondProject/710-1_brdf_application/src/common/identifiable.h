/*  System: BlueTruth Outstation
    Language/Build: MS VC 2008 / Linux GCC 4.2+
*/


#ifndef _IDENTIFIABLE_H_
#define _IDENTIFIABLE_H_

#include <string>
#include <vector>


/**
 * @brief This is an implementation of a class that enables
 * identification of the particular instance of the class.
 *
 * The instance of the class can have multiple identifiers.
 */
template <class T>
class Identifiable
{
public:
    //! Destructor.
    virtual ~Identifiable();

    //! Default constructor.
    Identifiable();
    //! Constructor with identifier
    Identifiable(const T& id);
    //! Copy constructor
    Identifiable(const Identifiable&);
    //! Assignment operator
    Identifiable& operator=(const Identifiable&);

    /**
     * Get the total number of identifiers
     * @return number of identifiers
     */
    size_t getNumberOfIdentifiers() const;

    /**
     * Check if this object has the provided identifier
     * @param id identifier to compare to
     * @return true if identifier found in its internal collection, false otherwise
     */
    bool isOfIdentifier(const T& id) const;

    /**
     * Add a new identifier
     * @param id an identifier to add
     */
    void addIdentifier(const T& id);

    /**
     * Remove a new identifier
     * @param id an identifier to remove
     */
    void removeIdentifier(const T& id);

    /**
     * Return a char array pointer containing all identifiers separated by comma
     * @return char array of identifiers
     */
    const char* getIdentifierName() const;

protected:
    typedef std::vector<T> TIdentifierCollection;
    TIdentifierCollection m_identifierCollection;

private:
    /**
     * Internal function to create a string containing all identifiers separated by comma
     */
    void updateName();

    std::string m_name;
};

#include "identifiable.hpp"


#endif //_IDENTIFIABLE_H_
