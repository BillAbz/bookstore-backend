
import { useEffect, useState } from 'react'
import './App.css'


function getUserIdFromToken(token) {
  try {
    const payload = JSON.parse(
        atob(
            token.split('.')[1]
                .replace(/-/g, '+')
                .replace(/_/g, '/')
        )
    )

    return Number(payload.sub)
  } catch {
    return null
  }
}

function getRoleFromToken(token) {
  try {
    const payload = JSON.parse(
        atob(
            token.split('.')[1]
                .replace(/-/g, '+')
                .replace(/_/g, '/')
        )
    )

    return payload.role
  } catch {
    return null
  }
}

function App() {
  const [books, setBooks] = useState([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')
  const [showLogin, setShowLogin] = useState(false)
  const [showRegister, setShowRegister] = useState(false)
  const [firstName, setFirstName] = useState('')
  const [lastName, setLastName] = useState('')
  const [registerError, setRegisterError] = useState('')
  const [registerSuccess, setRegisterSuccess] = useState('')
  const [email, setEmail] = useState('')
  const [password, setPassword] = useState('')
  const [token, setToken] = useState('')
  const [loginError, setLoginError] = useState('')
  const [reservationMessage, setReservationMessage] = useState('')
  const [reservingIsbn, setReservingIsbn] = useState(null)
  const [reservations, setReservations] = useState([])
  const [showReservations, setShowReservations] = useState(false)
  const [reservationLoading, setReservationLoading] = useState(false)
  const [selectedBookIsbn, setSelectedBookIsbn] = useState(null)
  const [reviews, setReviews] = useState([])
  const [reviewRating, setReviewRating] = useState(5)
  const [reviewComment, setReviewComment] = useState('')
  const [reviewMessage, setReviewMessage] = useState('')
  const [reviewLoading, setReviewLoading] = useState(false)
  const [showAdmin, setShowAdmin] = useState(false)
  const [adminMessage, setAdminMessage] = useState('')
  const [adminLoading, setAdminLoading] = useState(false)
  const [importIsbn, setImportIsbn] = useState('')
  const [importLoading, setImportLoading] = useState(false)
  const [importMessage, setImportMessage] = useState('')

  const [newBook, setNewBook] = useState({
    isbn: '',
    title: '',
    author: '',
    description: '',
    price: '',
    stock: ''
  })

  const isAdmin = token && getRoleFromToken(token) === 'ADMIN'
  const [editingIsbn, setEditingIsbn] = useState(null)


  useEffect(() => {
    fetch('/api/books')
        .then(response => {
          if (!response.ok) {
            throw new Error('Could not load books')
          }
          return response.json()
        })
        .then(data => setBooks(data))
        .catch(err => setError(err.message))
        .finally(() => setLoading(false))
  }, [])

  async function handleLogin(event) {
    event.preventDefault()
    setLoginError('')

    try {
      const response = await fetch('/api/users/login', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json'
        },
        body: JSON.stringify({ email, password })
      })

      if (!response.ok) {
        throw new Error('Invalid email or password')
      }

      const data = await response.json()
      setToken(data.accessToken)
      setPassword('')
      setShowLogin(false)

    } catch (err) {
      setLoginError(err.message)
    }
  }


  async function handleRegister(event) {
    event.preventDefault()
    setRegisterError('')
    setRegisterSuccess('')

    try {
      const response = await fetch('/api/users/register', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json'
        },
        body: JSON.stringify({
          firstName,
          lastName,
          email,
          password
        })
      })

      if (!response.ok) {
        if (response.status === 409) {
          throw new Error('This email is already registered')
        }
        throw new Error('Registration failed. Check your details.')
      }

      setRegisterSuccess('Registration successful! You can now log in.')
      setFirstName('')
      setLastName('')
      setPassword('')
      setShowRegister(false)
      setShowLogin(true)

    } catch (err) {
      setRegisterError(err.message)
    }
  }

  function handleLogout() {
    setToken('')
    setEmail('')
    setPassword('')
    setReservations([])
    setShowReservations(false)
    setShowAdmin(false)
    setAdminMessage('')
  }

  async function handleReservation(isbn) {
    if (!token) {
      setShowLogin(true)
      return
    }

    const userId = getUserIdFromToken(token)

    if (!userId) {
      setReservationMessage('Invalid session. Please log in again.')
      return
    }

    setReservingIsbn(isbn)
    setReservationMessage('')

    try {
      const response = await fetch('/api/reservations', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          'Authorization': `Bearer ${token}`
        },
        body: JSON.stringify({
          userId,
          isbn
        })
      })

      if (response.status === 401) {
        throw new Error('Your session has expired. Please log in again.')
      }

      if (!response.ok) {
        throw new Error('Reservation failed. The book may be out of stock.')
      }

      setReservationMessage('Book reserved successfully!')

      setBooks(currentBooks =>
          currentBooks.map(book =>
              book.isbn === isbn
                  ? { ...book, stock: book.stock - 1 }
                  : book
          )
      )

    } catch (err) {
      setReservationMessage(err.message)
    } finally {
      setReservingIsbn(null)
    }
  }


  async function loadReservations() {
    const userId = getUserIdFromToken(token)

    if (!token || !userId) return

    setReservationLoading(true)
    setReservationMessage('')

    try {
      const response = await fetch(
          `/api/reservations/user/${userId}`,
          {
            headers: {
              Authorization: `Bearer ${token}`
            }
          }
      )

      if (response.status === 401) {
        throw new Error('Your session has expired. Please log in again.')
      }

      if (!response.ok) {
        throw new Error('Could not load reservations.')
      }

      const data = await response.json()
      setReservations(data)
      setShowReservations(true)

    } catch (err) {
      setReservationMessage(err.message)
    } finally {
      setReservationLoading(false)
    }
  }


  async function cancelReservation(reservationId) {
    setReservationMessage('')

    try {
      const response = await fetch(
          `/api/reservations/${reservationId}`,
          {
            method: 'DELETE',
            headers: {
              Authorization: `Bearer ${token}`
            }
          }
      )

      if (!response.ok) {
        if (response.status === 401) {
          throw new Error('Your session has expired. Please log in again.')
        }

        throw new Error('Could not cancel reservation.')
      }

      setReservations(current =>
          current.filter(r => r.id !== reservationId)
      )

      setReservationMessage('Reservation cancelled successfully!')

      const booksResponse = await fetch('/api/books')

      if (booksResponse.ok) {
        setBooks(await booksResponse.json())
      }

    } catch (err) {
      setReservationMessage(err.message)
    }
  }


  async function loadReviews(isbn) {
    setSelectedBookIsbn(isbn)
    setReviewLoading(true)
    setReviewMessage('')
    setReviews([])

    try {
      const response = await fetch(`/api/reviews/book/${isbn}`)

      if (!response.ok) {
        throw new Error('Could not load reviews.')
      }

      setReviews(await response.json())
    } catch (err) {
      setReviewMessage(err.message)
    } finally {
      setReviewLoading(false)
    }
  }

  async function handleCreateReview(event) {
    event.preventDefault()

    if (!token || !selectedBookIsbn) {
      setReviewMessage('Please log in to write a review.')
      return
    }

    const userId = getUserIdFromToken(token)

    if (!userId) {
      setReviewMessage('Invalid session. Please log in again.')
      return
    }

    setReviewMessage('')

    try {
      const response = await fetch('/api/reviews', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          Authorization: `Bearer ${token}`
        },
        body: JSON.stringify({
          userId,
          isbn: selectedBookIsbn,
          rating: Number(reviewRating),
          comment: reviewComment
        })
      })

      if (response.status === 401) {
        throw new Error('Your session has expired. Please log in again.')
      }

      if (!response.ok) {
        throw new Error(
            'Could not submit review. You may have already reviewed this book.'
        )
      }

      setReviewComment('')
      setReviewRating(5)
      setReviewMessage('Review submitted successfully!')

      await refreshReviews(selectedBookIsbn)
    } catch (err) {
      setReviewMessage(err.message)
    }
  }

  async function refreshReviews(isbn) {
    const response = await fetch(`/api/reviews/book/${isbn}`)

    if (response.ok) {
      setReviews(await response.json())
    }
  }

  async function handleAddBook(event) {
    event.preventDefault()
    setAdminMessage('')
    setAdminLoading(true)

    try {
      const response = await fetch('/api/books', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          Authorization: `Bearer ${token}`
        },
        body: JSON.stringify({
          ...newBook,
          price: Number(newBook.price),
          stock: Number(newBook.stock)
        })
      })

      if (response.status === 401) {
        throw new Error('Session expired. Please log in again.')
      }

      if (response.status === 403) {
        throw new Error('Administrator access required.')
      }

      if (response.status === 409) {
        throw new Error('A book with this ISBN already exists.')
      }

      if (!response.ok) {
        throw new Error('Could not add book. Check the information.')
      }

      setAdminMessage('Book added successfully!')

      setNewBook({
        isbn: '',
        title: '',
        author: '',
        description: '',
        price: '',
        stock: ''
      })

      const booksResponse = await fetch('/api/books')

      if (booksResponse.ok) {
        setBooks(await booksResponse.json())
      }

    } catch (err) {
      setAdminMessage(err.message)
    } finally {
      setAdminLoading(false)
    }
  }

  function startEditingBook(book) {
    setEditingIsbn(book.isbn)
    setShowAdmin(true)
    setAdminMessage('')

    setNewBook({
      isbn: book.isbn,
      title: book.title ?? '',
      author: book.author ?? '',
      description: book.description ?? '',
      price: String(book.price),
      stock: String(book.stock)
    })

    window.scrollTo({ top: 0, behavior: 'smooth' })
  }

  function cancelEditingBook() {
    setEditingIsbn(null)
    setNewBook({
      isbn: '',
      title: '',
      author: '',
      description: '',
      price: '',
      stock: ''
    })
    setAdminMessage('')
  }

  async function handleUpdateBook(event) {
    event.preventDefault()

    if (!editingIsbn || !isAdmin) return

    setAdminMessage('')
    setAdminLoading(true)

    try {
      const response = await fetch(
          `/api/books/${encodeURIComponent(editingIsbn)}`,
          {
            method: 'PUT',
            headers: {
              'Content-Type': 'application/json',
              Authorization: `Bearer ${token}`
            },
            body: JSON.stringify({
              ...newBook,
              isbn: editingIsbn,
              price: Number(newBook.price),
              stock: Number(newBook.stock)
            })
          }
      )

      if (response.status === 401) {
        throw new Error('Session expired. Please log in again.')
      }

      if (response.status === 403) {
        throw new Error('Administrator access required.')
      }

      if (!response.ok) {
        throw new Error('Could not update book.')
      }

      const booksResponse = await fetch('/api/books')

      if (!booksResponse.ok) {
        throw new Error('Book updated, but the catalog could not be refreshed.')
      }

      setBooks(await booksResponse.json())
      cancelEditingBook()
      setAdminMessage('Book updated successfully!')

    } catch (err) {
      setAdminMessage(err.message)
    } finally {
      setAdminLoading(false)
    }
  }

  async function handleDeleteBook(book) {
    if (!isAdmin) return

    const confirmed = window.confirm(
        `Are you sure you want to delete "${book.title}"?`
    )

    if (!confirmed) return

    setAdminMessage('')
    setAdminLoading(true)

    try {
      const response = await fetch(
          `/api/books/${encodeURIComponent(book.isbn)}`,
          {
            method: 'DELETE',
            headers: {
              Authorization: `Bearer ${token}`
            }
          }
      )

      if (response.status === 401) {
        throw new Error('Session expired. Please log in again.')
      }

      if (response.status === 403) {
        throw new Error('Administrator access required.')
      }

      if (!response.ok) {
        throw new Error(
            'Could not delete book. It may have existing reservations or reviews.'
        )
      }

      const booksResponse = await fetch('/api/books')

      if (!booksResponse.ok) {
        throw new Error('Book deleted, but catalog refresh failed.')
      }

      setBooks(await booksResponse.json())
      setAdminMessage(`"${book.title}" deleted successfully.`)

      if (editingIsbn === book.isbn) {
        cancelEditingBook()
      }

    } catch (err) {
      setAdminMessage(err.message)
    } finally {
      setAdminLoading(false)
    }
  }

  async function handleImportBook(event) {
    event.preventDefault()

    if (!isAdmin) return

    const isbn = importIsbn.trim().replace(/[-\s]/g, '')

    if (!/^(?:[0-9]{10}|[0-9]{13})$/.test(isbn)) {
      setImportMessage('Please enter a valid 10 or 13 digit ISBN.')
      return
    }

    setImportMessage('')
    setImportLoading(true)

    try {
      const response = await fetch(
          `/api/books/import/${encodeURIComponent(isbn)}`,
          {
            method: 'POST',
            headers: {
              Authorization: `Bearer ${token}`
            }
          }
      )

      if (response.status === 401) {
        throw new Error('Session expired. Please log in again.')
      }

      if (response.status === 403) {
        throw new Error('Administrator access required.')
      }

      if (response.status === 404) {
        throw new Error('Book not found in Open Library.')
      }

      if (response.status === 409) {
        throw new Error('This book already exists in the bookstore.')
      }

      if (response.status === 503) {
        throw new Error('Open Library is temporarily unavailable.')
      }

      if (!response.ok) {
        throw new Error('Could not import book.')
      }

      const booksResponse = await fetch('/api/books')

      if (!booksResponse.ok) {
        throw new Error(
            'Book imported, but the catalog could not be refreshed.'
        )
      }

      setBooks(await booksResponse.json())
      setImportIsbn('')
      setImportMessage('Book imported successfully from Open Library!')

    } catch (err) {
      setImportMessage(err.message)
    } finally {
      setImportLoading(false)
    }
  }

  return (
      <div className="container">

        <header className="header">
          <h1>Bookstore</h1>

          <div>
            {token ? (
                <>
                  <span className="user-email">{email}</span>

                  {isAdmin && (
                      <button onClick={() => setShowAdmin(!showAdmin)}>
                        {showAdmin ? 'Close Admin Panel' : 'Admin Panel'}
                      </button>
                  )}

                  <button
                      onClick={loadReservations}
                      disabled={reservationLoading}
                  >
                    {reservationLoading ? 'Loading...' : 'My Reservations'}
                  </button>

                  <button onClick={handleLogout}>Logout</button>
                </>
            ) : (
                <>
                  <button onClick={() => {
                    setShowLogin(true)
                    setShowRegister(false)
                    setRegisterSuccess('')
                  }}>
                    Login
                  </button>

                  <button onClick={() => {
                    setShowRegister(true)
                    setShowLogin(false)
                    setRegisterSuccess('')
                  }}>
                    Register
                  </button>
                </>

            )}
          </div>
        </header>

        {showLogin && !token && (
            <div className="login-box">
              <h2>Login</h2>

              <form onSubmit={handleLogin}>
                <input
                    type="email"
                    placeholder="Email"
                    value={email}
                    onChange={e => setEmail(e.target.value)}
                    required
                />

                <input
                    type="password"
                    placeholder="Password"
                    value={password}
                    onChange={e => setPassword(e.target.value)}
                    required
                />

                <button type="submit">Login</button>
              </form>

              {loginError && (
                  <p className="error">{loginError}</p>
              )}
            </div>
        )}

        {showRegister && !token && (
            <div className="login-box">
              <h2>Create Account</h2>

              <form onSubmit={handleRegister}>
                <input
                    type="text"
                    placeholder="First Name"
                    value={firstName}
                    onChange={e => setFirstName(e.target.value)}
                    required
                />

                <input
                    type="text"
                    placeholder="Last Name"
                    value={lastName}
                    onChange={e => setLastName(e.target.value)}
                    required
                />

                <input
                    type="email"
                    placeholder="Email"
                    value={email}
                    onChange={e => setEmail(e.target.value)}
                    required
                />

                <input
                    type="password"
                    placeholder="Password"
                    value={password}
                    onChange={e => setPassword(e.target.value)}
                    required
                />

                <button type="submit">Create Account</button>
              </form>

              {registerError && (
                  <p className="error">{registerError}</p>
              )}
            </div>
        )}

        {registerSuccess && !token && (
            <p className="success">{registerSuccess}</p>
        )}


        {showReservations && token && (
            <section className="reservations-section">
              <div className="section-header">
                <h2>My Reservations</h2>

                <button onClick={() => setShowReservations(false)}>
                  Close
                </button>
              </div>

              {reservations.length === 0 ? (
                  <p>You have no reservations.</p>
              ) : (
                  <div className="reservation-list">
                    {reservations.map(reservation => (
                        <div className="reservation-card" key={reservation.id}>
                          <h3>{reservation.bookTitle}</h3>

                          <p><strong>ISBN:</strong> {reservation.isbn}</p>

                          <p>
                            <strong>Reserved:</strong>{' '}
                            {new Date(reservation.reservationDate).toLocaleString()}
                          </p>

                          <p>
                            <strong>Status:</strong>{' '}
                            {reservation.collected ? 'Collected' : 'Active'}
                          </p>

                          {!reservation.collected && (
                              <button
                                  className="cancel-button"
                                  onClick={() => cancelReservation(reservation.id)}
                              >
                                Cancel Reservation
                              </button>
                          )}
                        </div>
                    ))}
                  </div>
              )}
            </section>
        )}

        {isAdmin && showAdmin && (
            <section className="admin-section">
              <h2>Admin Panel</h2>
              <h3>{editingIsbn ? 'Edit Book' : 'Add New Book'}</h3>

              <div className="import-section">
                <h3>Import Book from Open Library</h3>

                <p>
                  Enter an ISBN to automatically import book information.
                  The price will be calculated automatically.
                </p>

                <form className="import-form" onSubmit={handleImportBook}>
                  <input
                      type="text"
                      placeholder="ISBN (10 or 13 digits)"
                      value={importIsbn}
                      onChange={e => setImportIsbn(e.target.value)}
                      required
                  />

                  <button type="submit" disabled={importLoading}>
                    {importLoading ? 'Importing...' : 'Import Book'}
                  </button>
                </form>

                {importMessage && (
                    <p role="status">{importMessage}</p>
                )}
              </div>

              <form
                  className="admin-form"
                  onSubmit={editingIsbn ? handleUpdateBook : handleAddBook}
              >
                <input
                    placeholder="ISBN"
                    value={newBook.isbn}
                    readOnly={Boolean(editingIsbn)}
                    onChange={e => setNewBook({
                      ...newBook,
                      isbn: e.target.value
                    })}
                    required
                />

                <input
                    placeholder="Book Title"
                    value={newBook.title}
                    onChange={e => setNewBook({
                      ...newBook,
                      title: e.target.value
                    })}
                    required
                />

                <input
                    placeholder="Author"
                    value={newBook.author}
                    onChange={e => setNewBook({
                      ...newBook,
                      author: e.target.value
                    })}
                    required
                />

                <textarea
                    placeholder="Description"
                    value={newBook.description}
                    onChange={e => setNewBook({
                      ...newBook,
                      description: e.target.value
                    })}
                    rows={3}
                />

                <input
                    type="number"
                    placeholder="Price (€)"
                    min="0"
                    step="0.01"
                    value={newBook.price}
                    onChange={e => setNewBook({
                      ...newBook,
                      price: e.target.value
                    })}
                    required
                />

                <input
                    type="number"
                    placeholder="Stock"
                    min="0"
                    step="1"
                    value={newBook.stock}
                    onChange={e => setNewBook({
                      ...newBook,
                      stock: e.target.value
                    })}
                    required
                />


                <button type="submit" disabled={adminLoading}>
                  {adminLoading
                      ? 'Saving...'
                      : editingIsbn
                          ? 'Save Changes'
                          : 'Add Book'}
                </button>

                {editingIsbn && (
                    <button
                        type="button"
                        className="cancel-edit-button"
                        onClick={cancelEditingBook}
                    >
                      Cancel Editing
                    </button>
                )}
              </form>

              {adminMessage && (
                  <p role="status">{adminMessage}</p>
              )}
            </section>
        )}

        <h2>Available Books</h2>

        {reservationMessage && (
            <p role="status">{reservationMessage}</p>
        )}

        {loading && <p>Loading books...</p>}
        {error && <p className="error">{error}</p>}

        {!loading && !error && books.length === 0 && (
            <p>No books available.</p>
        )}

        <div className="book-list">
          {books.map(book => (

              <div className="book-card" key={book.isbn}>
                <h3>{book.title}</h3>
                <p><strong>Author:</strong> {book.author}</p>
                <p><strong>ISBN:</strong> {book.isbn}</p>
                <p><strong>Price:</strong> €{book.price}</p>
                <p><strong>Stock:</strong> {book.stock}</p>

                <button
                    className="reserve-button"
                    onClick={() => handleReservation(book.isbn)}
                    disabled={book.stock <= 0 || reservingIsbn === book.isbn}
                >
                  {reservingIsbn === book.isbn
                      ? 'Reserving...'
                      : book.stock <= 0
                          ? 'Out of Stock'
                          : 'Reserve Book'}
                </button>
                <button
                    className="reviews-button"
                    onClick={() => loadReviews(book.isbn)}
                >
                  View Reviews
                </button>


                {isAdmin && (
                    <>
                      <button
                          className="edit-book-button"
                          onClick={() => startEditingBook(book)}
                          disabled={adminLoading}
                      >
                        Edit Book
                      </button>

                      <button
                          className="delete-book-button"
                          onClick={() => handleDeleteBook(book)}
                          disabled={adminLoading}
                      >
                        Delete Book
                      </button>
                    </>
                )}
              </div>

          ))}
        </div>



        {selectedBookIsbn && (
            <div
                className="modal-overlay"
                onClick={() => setSelectedBookIsbn(null)}
            >
              <section
                  className="reviews-section"
                  role="dialog"
                  aria-modal="true"
                  aria-label="Book reviews"
                  onClick={(event) => event.stopPropagation()}
              >
              <div className="section-header">
                <h2>Book Reviews</h2>
                <button onClick={() => setSelectedBookIsbn(null)}>
                  Close
                </button>
              </div>

              {reviewLoading ? (
                  <p>Loading reviews...</p>
              ) : (
                  <>
                    {reviews.length === 0 ? (
                        <p>No reviews yet.</p>
                    ) : (
                        <div className="review-list">
                          {reviews.map(review => (
                              <div className="review-card" key={review.id}>
                                <p><strong>Rating:</strong> {review.rating}/5</p>
                                <p>{review.comment}</p>
                                <small>
                                  {new Date(review.createdAt).toLocaleString()}
                                </small>
                              </div>
                          ))}
                        </div>
                    )}

                    {token && (
                        <form
                            className="review-form"
                            onSubmit={handleCreateReview}
                        >
                          <h3>Write a Review</h3>

                          <label htmlFor="review-rating">Rating</label>
                          <select
                              id="review-rating"
                              value={reviewRating}
                              onChange={e => setReviewRating(e.target.value)}
                          >
                            {[1, 2, 3, 4, 5].map(rating => (
                                <option key={rating} value={rating}>
                                  {rating} / 5
                                </option>
                            ))}
                          </select>

                          <label htmlFor="review-comment">Comment</label>
                          <textarea
                              id="review-comment"
                              value={reviewComment}
                              onChange={e => setReviewComment(e.target.value)}
                              maxLength={2000}
                              rows={4}
                          />

                          <button type="submit">Submit Review</button>
                        </form>
                    )}
                  </>
              )}

                {reviewMessage && (
                    <p role="status">{reviewMessage}</p>
                )}
              </section>
            </div>
        )}
        )

      </div>
  )
}

export default App
